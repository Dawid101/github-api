package com.dawid.github_api;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestClient;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest(
        classes = GithubApiApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@WireMockTest(httpPort = 8089)
class GithubApiIntegrationTest {

    @LocalServerPort
    private int port;

    private RestClient restClient;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("github.api.base-url", () -> "http://localhost:8089");
    }

    @BeforeEach
    void setUp() {
        restClient = RestClient.builder()
                .baseUrl("http://localhost:" + port)
                .build();
    }

    @Test
    void shouldReturnNonForkRepositoriesWithBranches() {
        //given
        StopWatch stopWatch = new StopWatch();
        stubFor(get(urlEqualTo("/users/testuser/repos"))
                .willReturn(okJson("""
                        [
                            {"name": "my-repo", "owner": {"login": "testuser"}, "fork": false},
                            {"name": "my-repo2", "owner": {"login": "testuser"}, "fork": false},
                            {"name": "forked-repo", "owner": {"login": "testuser"}, "fork": true}
                        ]
                        """)
                        .withFixedDelay(1000)));

        stubFor(get(urlEqualTo("/repos/testuser/my-repo/branches"))
                .willReturn(okJson("""
                        [
                            {"name": "main", "commit": {"sha": "abc123"}},
                            {"name": "develop", "commit": {"sha": "def456"}},
                            {"name": "feature", "commit": {"sha": "ghi789"}}
                        ]
                        """)
                        .withFixedDelay(1000)));
        stubFor(get(urlEqualTo("/repos/testuser/my-repo2/branches"))
                .willReturn(okJson("""
                        [
                            {"name": "main", "commit": {"sha": "ikl876"}},
                            {"name": "develop", "commit": {"sha": "xyz567"}},
                            {"name": "feature", "commit": {"sha": "jkl123"}}
                        ]
                        """)
                        .withFixedDelay(1000)));

        //when
        stopWatch.start();

        List<RepositoryResp> response = restClient.get()
                .uri("/api/testuser/repos")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });

        stopWatch.stop();

        //then
        assertThat(response).hasSize(2);

        RepositoryResp repo = response.getFirst();
        assertThat(repo.name()).isEqualTo("my-repo");
        assertThat(repo.ownerLogin()).isEqualTo("testuser");
        assertThat(repo.branches()).hasSize(3);
        assertThat(repo.branches().get(0).name()).isEqualTo("main");
        assertThat(repo.branches().get(0).sha()).isEqualTo("abc123");
        assertThat(repo.branches().get(1).name()).isEqualTo("develop");
        assertThat(repo.branches().get(1).sha()).isEqualTo("def456");
        assertThat(repo.branches().get(2).name()).isEqualTo("feature");
        assertThat(repo.branches().get(2).sha()).isEqualTo("ghi789");

        RepositoryResp repo2 = response.get(1);
        assertThat(repo2.name()).isEqualTo("my-repo2");
        assertThat(repo2.ownerLogin()).isEqualTo("testuser");
        assertThat(repo2.branches()).hasSize(3);
        assertThat(repo2.branches().get(0).name()).isEqualTo("main");
        assertThat(repo2.branches().get(0).sha()).isEqualTo("ikl876");
        assertThat(repo2.branches().get(1).name()).isEqualTo("develop");
        assertThat(repo2.branches().get(1).sha()).isEqualTo("xyz567");
        assertThat(repo2.branches().get(2).name()).isEqualTo("feature");
        assertThat(repo2.branches().get(2).sha()).isEqualTo("jkl123");


        verify(exactly(1), getRequestedFor(urlEqualTo("/users/testuser/repos")));
        verify(exactly(1), getRequestedFor(urlEqualTo("/repos/testuser/my-repo/branches")));
        verify(exactly(1), getRequestedFor(urlEqualTo("/repos/testuser/my-repo2/branches")));
        verify(exactly(0), getRequestedFor(urlEqualTo("/repos/testuser/forked-repo/branches")));
        verify(exactly(3), getRequestedFor(urlMatching(".*")));

        long totalTime = stopWatch.getTime();
        log.info("time " + totalTime);
        assertThat(totalTime).isGreaterThanOrEqualTo(3000).isLessThanOrEqualTo(3500);

    }

    @Test
    void shouldReturnNotFoundForNonExistentUser() {
        //given
        stubFor(get(urlEqualTo("/users/nonexistent/repos"))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {"message": "Not Found"}
                                """)));

        //when&then
        restClient.get()
                .uri("/api/nonexistent/repos")
                .exchange((request, response) -> {
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(404));

                    ErrorResp error = response.bodyTo(ErrorResp.class);
                    assertThat(error).isNotNull();
                    assertThat(error.status()).isEqualTo(404);
                    assertThat(error.message()).isEqualTo("Not Found");

                    return null;
                });
    }

    @Test
    void shouldReturnEmptyListWhenUserHasNoRepositories() {
        //given
        stubFor(get(urlEqualTo("/users/emptyuser/repos"))
                .willReturn(okJson("[]")));

        //when
        List<RepositoryResp> response = restClient.get()
                .uri("/api/emptyuser/repos")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });

        //then
        assertThat(response).isEmpty();

        verify(exactly(1), getRequestedFor(urlEqualTo("/users/emptyuser/repos")));
    }
}
