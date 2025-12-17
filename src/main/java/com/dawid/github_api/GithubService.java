package com.dawid.github_api;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GithubService {
    private final GithubClient githubClient;

    public List<Response> getRepositories(String username) {
        List<GithubRepository> repos = githubClient.getAllRepos(username);

        return repos.stream()
                .filter(repo -> !repo.fork())
                .map(repo -> {
                    List<Branch> branches = githubClient.getAllBranches(username, repo.name());

                    List<Response.BranchInfo> branchInfos = branches.stream()
                            .map(branch -> new Response.BranchInfo(branch.name(), branch.commit().sha()))
                            .toList();

                    return new Response(
                            repo.name(),
                            repo.owner().login(),
                            branchInfos
                    );
                })
                .toList();
    }
}
