package com.pjoter.proxy.controller

import com.pjoter.proxy.controller.response.RepositoryResponse
import com.pjoter.proxy.service.GitHubService
import kotlinx.coroutines.flow.toList
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/github")
class GitHubController(
    private val gitHubService: GitHubService,
) {
    @GetMapping(
        "/user/{username}/repository",
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    suspend fun listRepositoriesByUsername(
        @PathVariable username: String,
    ): ResponseEntity<List<RepositoryResponse>> {
        val repositories =
            gitHubService.fetchRepositoriesWithBranchesInfo(username)
                .toList()

        return ResponseEntity.ok(
            repositories,
        )
    }
}
