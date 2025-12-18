package com.dawid.github_api;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import wiremock.org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest()
@WireMockTest(httpPort = 8089)
class GithubServiceTest {

    @Autowired
    private GithubService githubService;

    @DynamicPropertySource
    static void configure(DynamicPropertyRegistry registry) {
        registry.add("https://api.github.com", () -> "http://localhost:8081");
    }

    @Test
    void should_return_correct_response() throws IOException {
        //given
        String responseBody = IOUtils.resourceToString("/wiremock/correct-response.json",
                StandardCharsets.UTF_8);

        List<Response.BranchInfo> branches = List.of(new Response.BranchInfo("main","541f17c609f484af93445fec0d86b1907aaac15f"));
        stubFor(get(urlEqualTo("/users/dawid101/repos"))
                .willReturn(
                        aResponse()
                                .withStatus(200)
                                .withHeader("Content-Type", "application/json")
                                .withBody(responseBody)
                )
        );

        //when
        Response response = new Response("cinema-reservation-app","Dawid101",branches);

        //then
        assertAll(() ->{
                assertEquals("cinema-reservation-app", response.name());
                assertEquals("Dawid101", response.ownerLogin());
        });
    }
}