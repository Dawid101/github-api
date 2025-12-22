package com.dawid.github_api;

public record ErrorResp(
        int status,
        String message
) {
}
