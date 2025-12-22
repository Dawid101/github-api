package com.dawid.github_api;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import java.util.List;

@HttpExchange
interface GithubClient {

    @GetExchange("/users/{username}/repos")
    List<GithubRepository> getAllRepos(@PathVariable String username);

    @GetExchange("/repos/{username}/{repoName}/branches")
    List<GithubBranch> getAllBranches(
            @PathVariable String username,
            @PathVariable String repoName
    );
}
