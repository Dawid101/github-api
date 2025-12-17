package com.dawid.github_api;

import java.util.List;

public record Response(
        String name,
        String ownerLogin,
        List<BranchInfo> branches
) {
    record BranchInfo(
            String name,
            String sha
    ){}
}
