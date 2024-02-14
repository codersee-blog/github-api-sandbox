package com.pjoter.proxy.api.github

import com.pjoter.proxy.api.common.UpstreamApiException
import com.pjoter.proxy.api.github.model.GitHubBranchResponse
import com.pjoter.proxy.api.github.model.GitHubRepoResponse
import com.pjoter.proxy.api.github.model.PageableGitHubResponse
import com.pjoter.proxy.api.github.util.checkIfMorePagesToFetch
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import org.springframework.web.reactive.function.client.awaitExchangeOrNull

@Component
class GitHubApiImpl(
    private val webClient: WebClient,
) : GitHubApi {
    override suspend fun listRepositoriesByUsername(
        username: String,
        page: Int,
        perPage: Int,
    ): PageableGitHubResponse<GitHubRepoResponse>? =
        webClient.get()
            .uri("/users/$username/repos?page=$page&per_page=$perPage")
            .awaitExchangeOrNull(::mapToPageableResponse)

    override suspend fun listBranchesByUsernameAndRepositoryName(
        username: String,
        repositoryName: String,
        page: Int,
        perPage: Int,
    ): PageableGitHubResponse<GitHubBranchResponse>? =
        webClient.get()
            .uri("/repos/$username/$repositoryName/branches?page=$page&per_page=$perPage")
            .awaitExchangeOrNull(::mapToPageableResponse)

    private suspend inline fun <reified T> mapToPageableResponse(clientResponse: ClientResponse): PageableGitHubResponse<T>? {
        val hasNext = checkIfMorePagesToFetch(clientResponse)

        return when (val statusCode = clientResponse.statusCode()) {
            HttpStatus.OK ->
                PageableGitHubResponse(
                    items = clientResponse.awaitBody<List<T>>(),
                    hasMoreItems = hasNext,
                )

            HttpStatus.NOT_FOUND -> null

            else -> throw UpstreamApiException(
                msg = "GitHub API request failed.",
                statusCode = statusCode,
            )
        }
    }
}
