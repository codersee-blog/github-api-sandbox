package com.pjoter.proxy.api.github

import com.pjoter.proxy.api.github.model.GitHubBranchResponse
import com.pjoter.proxy.api.github.model.GitHubRepoResponse
import com.pjoter.proxy.api.github.model.PageableGitHubResponse

interface GitHubApi {
    suspend fun listRepositoriesByUsername(
        username: String,
        page: Int = 1,
        perPage: Int = 30,
    ): PageableGitHubResponse<GitHubRepoResponse>?

    suspend fun listBranchesByUsernameAndRepositoryName(
        username: String,
        repositoryName: String,
        page: Int = 1,
        perPage: Int = 30,
    ): PageableGitHubResponse<GitHubBranchResponse>?
}
