package com.dawid.github_api;

public record Branch(
        String name,
        Commit commit
) {
    record Commit(
            String sha
    ){}
}
