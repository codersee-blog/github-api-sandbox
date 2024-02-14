package com.pjoter.proxy.api.github.model

data class GitHubBranchResponse(
    val name: String,
    val commit: GitHubCommitResponse,
)
