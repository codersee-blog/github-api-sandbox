package com.pjoter.proxy.api.github.model

data class GitHubRepoResponse(
    val fork: Boolean,
    val name: String,
    val owner: GitHubOwnerResponse,
)
