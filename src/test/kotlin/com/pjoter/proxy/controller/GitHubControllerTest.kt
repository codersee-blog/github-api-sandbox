package com.pjoter.proxy.controller

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.pjoter.proxy.controller.response.BranchResponse
import com.pjoter.proxy.controller.response.ErrorResponse
import com.pjoter.proxy.controller.response.RepositoryOwnerResponse
import com.pjoter.proxy.controller.response.RepositoryResponse
import com.pjoter.proxy.util.getResponseBodyAsString
import io.netty.handler.codec.http.HttpHeaderValues
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBodyList
import java.util.UUID

private const val TEST_KEY = "TEST_KEY"
private const val TEST_PORT = 8082
private const val TEST_VERSION = "2022-11-28"

@AutoConfigureWireMock(port = TEST_PORT)
@TestPropertySource(
    properties = [
        "api.github.url=http://localhost:${TEST_PORT}",
        "api.github.key=$TEST_KEY",
        "api.github.version=$TEST_VERSION",
    ],
)
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
)
class GitHubControllerTest {
    @Autowired
    private lateinit var wireMockServer: WireMockServer

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @Test
    fun `Given 200 OK response with empty list from GitHub When getting repositories by username Then should return empty list`() =
        runTest {
            // Given
            val page = 1
            val perPage = 30
            val username = UUID.randomUUID().toString()
            val linkHeader = """<https://api.github.com/user/64011387/repos?page=3&per_page=2>; rel="prev","""

            wireMockServer.stubFor(
                WireMock.get(WireMock.urlEqualTo("/users/$username/repos?page=$page&per_page=$perPage"))
                    .withHeader("Authorization", WireMock.equalTo("Bearer $TEST_KEY"))
                    .withHeader("X-GitHub-Api-Version", WireMock.equalTo(TEST_VERSION))
                    .withHeader("Accept", WireMock.equalTo("application/vnd.github+json"))
                    .willReturn(
                        WireMock.aResponse()
                            .withStatus(HttpStatus.OK.value())
                            .withHeader(HttpHeaders.CONTENT_TYPE, "application/json; charset=utf-8")
                            .withHeader(HttpHeaders.LINK, linkHeader)
                            .withBody(
                                getResponseBodyAsString("/responses/external/github/list_github_repositories_200_OK_empty_list.json"),
                            ),
                    ),
            )

            // Then
            webTestClient.get()
                .uri("/github/user/$username/repository")
                .header("Accept", "application/json")
                .exchange()
                .expectStatus()
                .isOk
                .expectBodyList(RepositoryResponse::class.java)
                .hasSize(0)
        }

    @Test
    fun `Given 200 OK response from GitHub When getting repositories by username Then should return list with mapped items`() =
        runTest {
            // Given
            val page = 1
            val perPage = 30
            val username = UUID.randomUUID().toString()
            val linkHeader = """<https://api.github.com/user/64011387/repos?page=3&per_page=2>; rel="prev","""

            wireMockServer.stubFor(
                WireMock.get(WireMock.urlEqualTo("/users/$username/repos?page=$page&per_page=$perPage"))
                    .withHeader("Authorization", WireMock.equalTo("Bearer $TEST_KEY"))
                    .withHeader("X-GitHub-Api-Version", WireMock.equalTo(TEST_VERSION))
                    .withHeader("Accept", WireMock.equalTo("application/vnd.github+json"))
                    .willReturn(
                        WireMock.aResponse()
                            .withStatus(HttpStatus.OK.value())
                            .withHeader(HttpHeaders.CONTENT_TYPE, "application/json; charset=utf-8")
                            .withHeader(HttpHeaders.LINK, linkHeader)
                            .withBody(
                                getResponseBodyAsString("/responses/external/github/list_github_repositories_200_OK_page_1.json"),
                            ),
                    ),
            )

            wireMockServer.stubFor(
                WireMock.get(
                    WireMock.urlEqualTo("/repos/$username/controlleradvice-vs-restcontrolleradvice/branches?page=$page&per_page=$perPage"),
                )
                    .withHeader("Authorization", WireMock.equalTo("Bearer $TEST_KEY"))
                    .withHeader("X-GitHub-Api-Version", WireMock.equalTo(TEST_VERSION))
                    .withHeader("Accept", WireMock.equalTo("application/vnd.github+json"))
                    .willReturn(
                        WireMock.aResponse()
                            .withStatus(HttpStatus.OK.value())
                            .withHeader(HttpHeaders.CONTENT_TYPE, "application/json; charset=utf-8")
                            .withBody(
                                getResponseBodyAsString("/responses/external/github/list_repo_one_branches_200_OK.json"),
                            ),
                    ),
            )

            wireMockServer.stubFor(
                WireMock.get(
                    WireMock.urlEqualTo("/repos/$username/freecodecamp-spring-boot-kotlin-excel/branches?page=$page&per_page=$perPage"),
                )
                    .withHeader("Authorization", WireMock.equalTo("Bearer $TEST_KEY"))
                    .withHeader("X-GitHub-Api-Version", WireMock.equalTo(TEST_VERSION))
                    .withHeader("Accept", WireMock.equalTo("application/vnd.github+json"))
                    .willReturn(
                        WireMock.aResponse()
                            .withStatus(HttpStatus.OK.value())
                            .withHeader(HttpHeaders.CONTENT_TYPE, "application/json; charset=utf-8")
                            .withBody(
                                getResponseBodyAsString("/responses/external/github/list_repo_two_branches_200_OK.json"),
                            ),
                    ),
            )

            // Then
            val firstExpected =
                RepositoryResponse(
                    name = "controlleradvice-vs-restcontrolleradvice",
                    owner =
                        RepositoryOwnerResponse(
                            login = "codersee-blog",
                        ),
                    branches =
                        listOf(
                            BranchResponse(
                                name = "main",
                                sha = "bf7bb469120121b4cf1ec35bff3a3928b478f6ad",
                            ),
                        ),
                )

            val secondExpected =
                RepositoryResponse(
                    name = "freecodecamp-spring-boot-kotlin-excel",
                    owner =
                        RepositoryOwnerResponse(
                            login = "codersee-blog",
                        ),
                    branches =
                        listOf(
                            BranchResponse(
                                name = "master",
                                sha = "03aa4d08a6d60d2a448eae6346d737532d7289a4",
                            ),
                        ),
                )

            webTestClient.get()
                .uri("/github/user/$username/repository")
                .header("Accept", "application/json")
                .exchange()
                .expectStatus()
                .isOk
                .expectBodyList<RepositoryResponse>()
                .contains(firstExpected)
                .contains(secondExpected)
        }

    @Test
    fun `Given non existing username When getting repositories from GitHub by username Then should return 404 NOT FOUND`() =
        runTest {
            // Given
            val page = 1
            val perPage = 30
            val username = UUID.randomUUID().toString()

            wireMockServer.stubFor(
                WireMock.get(WireMock.urlEqualTo("/users/$username/repos?page=$page&per_page=$perPage"))
                    .withHeader("Authorization", WireMock.equalTo("Bearer $TEST_KEY"))
                    .withHeader("X-GitHub-Api-Version", WireMock.equalTo(TEST_VERSION))
                    .withHeader("Accept", WireMock.equalTo("application/vnd.github+json"))
                    .willReturn(
                        WireMock.aResponse()
                            .withStatus(HttpStatus.NOT_FOUND.value())
                            .withHeader(HttpHeaders.CONTENT_TYPE, "application/json; charset=utf-8")
                            .withBody(
                                getResponseBodyAsString("/responses/external/github/list_github_repositories_404_NOT_FOUND.json"),
                            ),
                    ),
            )

            // Then
            val expected =
                ErrorResponse(
                    status = 404,
                    message = "GitHub user with username $username was not found.",
                )

            webTestClient.get()
                .uri("/github/user/$username/repository")
                .header("Accept", "application/json")
                .exchange()
                .expectStatus()
                .isNotFound
                .expectBody(ErrorResponse::class.java)
                .isEqualTo(expected)
        }

    @Test
    fun `Given request with accept application xml header When getting repositories from GitHub by username Then should return 406 NOT ACCEPTABLE`() =
        runTest {
            val expected =
                ErrorResponse(
                    status = 406,
                    message = "Invalid 'Accept' header value.",
                )

            webTestClient.get()
                .uri("/github/user/any/repository")
                .header(HttpHeaders.ACCEPT, HttpHeaderValues.APPLICATION_XML.toString())
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.NOT_ACCEPTABLE)
                .expectBody(ErrorResponse::class.java)
                .isEqualTo(expected)
        }

    @Test
    fun `Given 401 Unauthorized response from GitHub When getting repositories by username Then should return 424 FAILED DEPENDENCY`() =
        runTest {
            // Given
            val page = 1
            val perPage = 30
            val username = UUID.randomUUID().toString()

            wireMockServer.stubFor(
                WireMock.get(WireMock.urlEqualTo("/users/$username/repos?page=$page&per_page=$perPage"))
                    .withHeader("Authorization", WireMock.equalTo("Bearer $TEST_KEY"))
                    .withHeader("X-GitHub-Api-Version", WireMock.equalTo(TEST_VERSION))
                    .withHeader("Accept", WireMock.equalTo("application/vnd.github+json"))
                    .willReturn(
                        WireMock.aResponse()
                            .withStatus(HttpStatus.UNAUTHORIZED.value()),
                    ),
            )

            // Then
            val expected =
                ErrorResponse(
                    status = 424,
                    message = "The request to GitHub API failed.",
                )

            webTestClient.get()
                .uri("/github/user/$username/repository")
                .header("Accept", "application/json")
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.FAILED_DEPENDENCY)
                .expectBody(ErrorResponse::class.java)
                .isEqualTo(expected)
        }

    // TODO Could be more cases
}
