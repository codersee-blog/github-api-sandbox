package com.pjoter.proxy.service

import com.pjoter.proxy.controller.response.RepositoryResponse
import kotlinx.coroutines.flow.Flow

interface GitHubService {
    suspend fun fetchRepositoriesWithBranchesInfo(username: String): Flow<RepositoryResponse>
}
