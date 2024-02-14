package com.pjoter.proxy.service

import com.pjoter.proxy.api.github.model.GitHubBranchResponse
import com.pjoter.proxy.api.github.model.GitHubRepoResponse
import com.pjoter.proxy.controller.response.BranchResponse
import com.pjoter.proxy.controller.response.RepositoryOwnerResponse
import com.pjoter.proxy.controller.response.RepositoryResponse
import com.pjoter.proxy.provider.GitHubDataProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.map
import org.springframework.stereotype.Service

@Service
class GitHubServiceImpl(
    private val gitHubDataProvider: GitHubDataProvider,
) : GitHubService {
    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun fetchRepositoriesWithBranchesInfo(username: String): Flow<RepositoryResponse> =
        gitHubDataProvider.fetchAllNotForkedRepositoriesByUsername(username)
            .flatMapMerge { repo ->
                gitHubDataProvider.fetchAllBranchesForRepository(username, repo.name)
                    .mapToResponse(repo)
            }

    private fun Flow<List<GitHubBranchResponse>>.mapToResponse(repo: GitHubRepoResponse): Flow<RepositoryResponse> =
        this.map { branches ->
            RepositoryResponse(
                name = repo.name,
                owner =
                    RepositoryOwnerResponse(
                        login = repo.owner.login,
                    ),
                branches =
                    branches.map { branch ->
                        BranchResponse(
                            name = branch.name,
                            sha = branch.commit.sha,
                        )
                    },
            )
        }
}
