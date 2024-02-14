package com.pjoter.proxy.api.common

import org.springframework.http.HttpStatusCode

data class UpstreamApiException(
    val msg: String,
    val statusCode: HttpStatusCode,
) : RuntimeException(msg)
