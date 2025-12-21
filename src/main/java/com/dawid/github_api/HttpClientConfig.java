package com.dawid.github_api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class HttpClientConfig {
    @Value("${github.api.base-url:https://api.github.com}")
    private String githubApiBaseUrl;

    @Bean
    public GithubClient githubClient() {
        RestClient restClient = RestClient.builder()
                .baseUrl(githubApiBaseUrl)
                .build();

        HttpServiceProxyFactory factory = HttpServiceProxyFactory
                .builderFor(RestClientAdapter.create(restClient))
                .build();

        return factory.createClient(GithubClient.class);
    }

}
