package com.pjoter.proxy.controller.response

data class RepositoryResponse(
    val name: String,
    val owner: RepositoryOwnerResponse,
    val branches: List<BranchResponse>,
)
