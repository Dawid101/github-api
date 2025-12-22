package com.dawid.github_api;

record GithubBranch(
        String name,
        Commit commit
) {
    record Commit(
            String sha
    ){}
}
