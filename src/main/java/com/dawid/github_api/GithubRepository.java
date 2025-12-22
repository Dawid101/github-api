package com.dawid.github_api;


record GithubRepository(
        String name,
        Owner owner,
        boolean fork
) {
    record Owner (
      String login
    ){}
}
