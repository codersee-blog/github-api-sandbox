package com.pjoter.proxy.provider

import com.pjoter.proxy.api.github.GitHubApi
import com.pjoter.proxy.api.github.model.GitHubBranchResponse
import com.pjoter.proxy.api.github.model.GitHubRepoResponse
import com.pjoter.proxy.api.github.model.PageableGitHubResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import org.springframework.stereotype.Component

@Component
class GitHubDataProviderImpl(
    private val gitHubApi: GitHubApi,
) : GitHubDataProvider {
    override suspend fun fetchAllNotForkedRepositoriesByUsername(username: String): Flow<GitHubRepoResponse> =
        flow {
            val firstRepositoriesChunk =
                gitHubApi.listRepositoriesByUsername(
                    username = username,
                    page = 1,
                )

            firstRepositoriesChunk?.let { firstChunk ->
                emitNotForkedRepositories(firstChunk)

                if (firstRepositoriesChunk.hasMoreItems) {
                    fetchAndEmitRemainingRepositories(username)
                }
            } ?: throw UserNotFoundException("GitHub user with username $username was not found.")
        }

    private suspend fun FlowCollector<GitHubRepoResponse>.fetchAndEmitRemainingRepositories(username: String) {
        var page = 2
        do {
            val responses =
                gitHubApi.listRepositoriesByUsername(
                    username = username,
                    page = page++,
                )

            emitNotForkedRepositories(responses)
        } while (responses?.hasMoreItems == true)
    }

    override suspend fun fetchAllBranchesForRepository(
        username: String,
        repositoryName: String,
    ): Flow<List<GitHubBranchResponse>> =
        flow {
            var page = 1
            do {
                val pageableBranchesResponse =
                    gitHubApi.listBranchesByUsernameAndRepositoryName(
                        username = username,
                        repositoryName = repositoryName,
                        page = page++,
                    )

                emitBranches(pageableBranchesResponse)
            } while (pageableBranchesResponse?.hasMoreItems == true)
        }

    private suspend fun FlowCollector<GitHubRepoResponse>.emitNotForkedRepositories(
        responses: PageableGitHubResponse<GitHubRepoResponse>?,
    ) {
        responses?.items
            ?.filterNot { it.fork }
            ?.forEach { emit(it) }
    }

    private suspend fun FlowCollector<List<GitHubBranchResponse>>.emitBranches(responses: PageableGitHubResponse<GitHubBranchResponse>?) {
        responses
            ?.items
            ?.let { emit(it) }
    }
}

data class UserNotFoundException(
    val msg: String,
) : RuntimeException(msg)
