package com.pjoter.proxy.provider

import com.pjoter.proxy.api.github.model.GitHubBranchResponse
import com.pjoter.proxy.api.github.model.GitHubRepoResponse
import kotlinx.coroutines.flow.Flow

interface GitHubDataProvider {
    suspend fun fetchAllNotForkedRepositoriesByUsername(username: String): Flow<GitHubRepoResponse>

    suspend fun fetchAllBranchesForRepository(
        username: String,
        repositoryName: String,
    ): Flow<List<GitHubBranchResponse>>
}
