package com.pjoter.proxy.provider

import com.pjoter.proxy.api.github.GitHubApi
import com.pjoter.proxy.api.github.model.GitHubOwnerResponse
import com.pjoter.proxy.api.github.model.GitHubRepoResponse
import com.pjoter.proxy.api.github.model.PageableGitHubResponse
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.UUID

class GitHubDataProviderImplTest {
    private val gitHubApi: GitHubApi = mockk()

    private val provider = GitHubDataProviderImpl(gitHubApi)

    @Test
    fun `Given null response for the first repositories chunk When fetching not forked repositories Then should throw UserNotFoundException`() =
        runTest {
            // Given
            val username = UUID.randomUUID().toString()
            val page = 1
            val perPage = 30

            coEvery {
                gitHubApi.listRepositoriesByUsername(
                    username = username,
                    page = page,
                    perPage = perPage,
                )
            } returns null

            // When
            val exception =
                assertThrows<UserNotFoundException> {
                    provider.fetchAllNotForkedRepositoriesByUsername(username)
                        .toList()
                }

            // Then
            assertNotNull(exception)

            assertEquals("GitHub user with username $username was not found.", exception.msg)
        }

    @Test
    fun `Given response without more items When fetching not forked repositories Then return only one chunk without forked repositories`() =
        runTest {
            // Given
            val username = UUID.randomUUID().toString()
            val page = 1
            val perPage = 30

            val response =
                PageableGitHubResponse(
                    items =
                        listOf(
                            GitHubRepoResponse(
                                fork = false,
                                name = "controlleradvice-vs-restcontrolleradvice",
                                owner = GitHubOwnerResponse(login = "codersee-blog"),
                            ),
                            GitHubRepoResponse(
                                fork = true,
                                name = "freecodecamp-spring-boot-kotlin-excel",
                                owner = GitHubOwnerResponse(login = "codersee-blog"),
                            ),
                        ),
                    hasMoreItems = false,
                )

            coEvery {
                gitHubApi.listRepositoriesByUsername(
                    username = username,
                    page = page,
                    perPage = perPage,
                )
            } returns response

            // When
            val result =
                provider.fetchAllNotForkedRepositoriesByUsername(username)
                    .toList()

            // Then
            val expected =
                listOf(
                    GitHubRepoResponse(
                        fork = false,
                        name = "controlleradvice-vs-restcontrolleradvice",
                        owner = GitHubOwnerResponse(login = "codersee-blog"),
                    ),
                )

            assertEquals(expected, result)
        }

    @Test
    fun `Given response with more items When fetching not forked repositories Then return all not forked repositories`() =
        runTest {
            // Given
            val username = UUID.randomUUID().toString()

            val firstResponse =
                PageableGitHubResponse(
                    items =
                        listOf(
                            GitHubRepoResponse(
                                fork = false,
                                name = "controlleradvice-vs-restcontrolleradvice",
                                owner = GitHubOwnerResponse(login = "codersee-blog"),
                            ),
                            GitHubRepoResponse(
                                fork = true,
                                name = "freecodecamp-spring-boot-kotlin-excel",
                                owner = GitHubOwnerResponse(login = "codersee-blog"),
                            ),
                        ),
                    hasMoreItems = true,
                )

            val secondResponse =
                PageableGitHubResponse(
                    items =
                        listOf(
                            GitHubRepoResponse(
                                fork = false,
                                name = "another-repo",
                                owner = GitHubOwnerResponse(login = "codersee-blog"),
                            ),
                            GitHubRepoResponse(
                                fork = true,
                                name = "another-repo-2",
                                owner = GitHubOwnerResponse(login = "codersee-blog"),
                            ),
                        ),
                    hasMoreItems = false,
                )

            coEvery {
                gitHubApi.listRepositoriesByUsername(
                    username = username,
                    page = 1,
                    perPage = 30,
                )
            } returns firstResponse

            coEvery {
                gitHubApi.listRepositoriesByUsername(
                    username = username,
                    page = 2,
                    perPage = 30,
                )
            } returns secondResponse

            // When
            val result =
                provider.fetchAllNotForkedRepositoriesByUsername(username)
                    .toList()

            // Then
            val expected =
                listOf(
                    GitHubRepoResponse(
                        fork = false,
                        name = "controlleradvice-vs-restcontrolleradvice",
                        owner = GitHubOwnerResponse(login = "codersee-blog"),
                    ),
                    GitHubRepoResponse(
                        fork = false,
                        name = "another-repo",
                        owner = GitHubOwnerResponse(login = "codersee-blog"),
                    ),
                )

            assertEquals(expected, result)
        }

    // TODO same for the `fetchAllBranchesForRepository`
}
