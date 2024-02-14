package com.pjoter.proxy.api.github.model

data class PageableGitHubResponse<T>(
    val items: List<T>,
    val hasMoreItems: Boolean,
)
