package com.dawid.github_api;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest()
@ActiveProfiles("test")
@WireMockTest(httpPort = 8089)
class GithubServiceIntegrationTest {
    @Autowired
    private GithubService githubService;


    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("github.api.base-url", () -> "http://localhost:8089");
    }

    @Test
    void shouldReturnRepositoriesWithBranches() {
        // GIVEN - Stub dla repos
        stubFor(get(urlEqualTo("/users/dawid101/repos"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                [
                                  {
                                    "name": "cinema-reservation-app",
                                    "owner": {
                                      "login": "Dawid101"
                                    },
                                    "fork": false
                                  }
                                ]
                                """)));

        // GIVEN
        stubFor(get(urlEqualTo("/repos/dawid101/cinema-reservation-app/branches"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                [
                                  {
                                    "name": "main",
                                    "commit": {
                                      "sha": "541f17c609f484af93445fec0d86b1907aaac15f"
                                    }
                                  }
                                ]
                                """)));

        // WHEN
        List<Response> result = githubService.getRepositories("dawid101");

        // THEN
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().name()).isEqualTo("cinema-reservation-app");
        assertThat(result.getFirst().ownerLogin()).isEqualTo("Dawid101");
        assertThat(result.getFirst().branches()).hasSize(1);
        assertThat(result.getFirst().branches().getFirst().name()).isEqualTo("main");

        verify(exactly(1), getRequestedFor(urlEqualTo("/users/dawid101/repos")));
        verify(exactly(1), getRequestedFor(
                urlEqualTo("/repos/dawid101/cinema-reservation-app/branches")
        ));
    }

    @Test
    void shouldFilterOutForkedRepositories() {
        // GIVEN
        stubFor(get(urlEqualTo("/users/dawid101/repos"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                [
                                  {
                                    "name": "original-repo",
                                    "owner": {
                                      "login": "dawid101"
                                    },
                                    "fork": false
                                  },
                                  {
                                    "name": "another-original",
                                    "owner": {
                                      "login": "dawid101"
                                    },
                                    "fork": false
                                  },
                                  {
                                    "name": "forked-repo",
                                    "owner": {
                                      "login": "dawid101"
                                    },
                                    "fork": true
                                  }
                                ]
                                """)));

        stubFor(get(urlEqualTo("/repos/dawid101/original-repo/branches"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                [
                                  {
                                    "name": "main",
                                    "commit": {
                                      "sha": "qwer123"
                                    }
                                  }
                                ]
                                """)));

        stubFor(get(urlEqualTo("/repos/dawid101/another-original/branches"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                [
                                  {
                                    "name": "main",
                                    "commit": {
                                      "sha": "xyz2121"
                                    }
                                  }
                                ]
                                """)));

        // WHEN
        List<Response> result = githubService.getRepositories("dawid101");

        // THEN
        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(Response::name)
                .containsExactly("original-repo", "another-original")
                .doesNotContain("forked-repo");

        verify(exactly(1), getRequestedFor(urlEqualTo("/users/dawid101/repos")));
        verify(exactly(1), getRequestedFor(urlEqualTo("/repos/dawid101/original-repo/branches")));
        verify(exactly(1), getRequestedFor(urlEqualTo("/repos/dawid101/another-original/branches")));
        verify(exactly(0), getRequestedFor(urlEqualTo("/repos/dawid101/forked-repo/branches")));
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        stubFor(get(urlEqualTo("/users/nonexistent/repos"))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {"message": "Not Found"}
                                """)));

        assertThatThrownBy(() -> githubService.getRepositories("nonexistent"))
                .isInstanceOf(HttpClientErrorException.NotFound.class)
                .hasMessageContaining("404");
    }

    @Test
    void shouldReturnEmptyListWhenUserHasNoRepositories() {
        // GIVEN
        stubFor(get(urlEqualTo("/users/emptyuser/repos"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("[]")));

        // WHEN
        List<Response> result = githubService.getRepositories("emptyuser");

        // THEN
        assertThat(result).isEmpty();
        assertThat(result).hasSize(0);

        verify(exactly(1), getRequestedFor(urlEqualTo("/users/emptyuser/repos")));
        verify(exactly(0), getRequestedFor(urlMatching("/repos/.*/branches")));
    }

}