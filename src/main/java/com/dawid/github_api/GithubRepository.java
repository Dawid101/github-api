package com.dawid.github_api;


public record GithubRepository(
        String name,
        Owner owner,
        boolean fork
) {
    record Owner (
      String login
    ){}
}
