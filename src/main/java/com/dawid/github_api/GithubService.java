package com.dawid.github_api;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
class GithubService {
    private final GithubClient githubClient;

    List<RepositoryResp> getUserRepositories(String username) {
        return githubClient.getAllRepos(username).stream()
                .filter(repo -> !repo.fork())
                .map(repo -> toRepositoryResponse(username,repo))
                .toList();
    }

    private RepositoryResp toRepositoryResponse(String username, GithubRepository repository){
        List<RepositoryResp.BranchInfo> branchInfos = githubClient.getAllBranches(username,repository.name()).stream()
                .map(branch -> new RepositoryResp.BranchInfo(branch.name(), branch.commit().sha()))
                .toList();
        return new RepositoryResp(repository.name(), repository.owner().login(),branchInfos);
    }
}
