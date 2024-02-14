package com.pjoter.proxy.controller.advice

import com.pjoter.proxy.api.common.UpstreamApiException
import com.pjoter.proxy.controller.response.ErrorResponse
import com.pjoter.proxy.provider.UserNotFoundException
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.server.NotAcceptableStatusException

@RestControllerAdvice
class ExceptionHandlers {
    @ExceptionHandler(NotAcceptableStatusException::class)
    fun handle(): ResponseEntity<ErrorResponse> {
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON

        val errorResponse =
            ErrorResponse(
                status = 406,
                message = "Invalid 'Accept' header value.",
            )

        return ResponseEntity(errorResponse, headers, HttpStatus.NOT_ACCEPTABLE)
    }

    @ExceptionHandler(UserNotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleUserNotFoundException(ex: UserNotFoundException): ErrorResponse =
        ErrorResponse(
            status = 404,
            message = ex.msg,
        )

    @ExceptionHandler(UpstreamApiException::class)
    @ResponseStatus(HttpStatus.FAILED_DEPENDENCY)
    fun handleFailedDependencyException(): ErrorResponse =
        ErrorResponse(
            status = 424,
            message = "The request to GitHub API failed.",
        )
}
