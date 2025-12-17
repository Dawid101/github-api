package com.dawid.github_api;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.service.registry.ImportHttpServices;

@Configuration
@ImportHttpServices(basePackages = "com.dawid.github-api",
        types = {GithubClient.class})
public class HttpClientConfig {
}
