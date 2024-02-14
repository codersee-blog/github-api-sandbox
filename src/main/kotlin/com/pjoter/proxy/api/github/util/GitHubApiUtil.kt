package com.pjoter.proxy.api.github.util

import org.springframework.web.reactive.function.client.ClientResponse

fun checkIfMorePagesToFetch(clientResponse: ClientResponse) =
    clientResponse.headers()
        .header("link")
        .firstOrNull()
        ?.contains("next")
        ?: false
