package com.sklad.skladproject.controllers

import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

// Validation problems (IllegalArgumentException) are the client's fault -> 400 with the reason.
// Anything else came from the database -> 500 with a generic message, details stay in the log.
fun Throwable.toResponseStatusException(): ResponseStatusException = when (this) {
    is IllegalArgumentException -> ResponseStatusException(HttpStatus.BAD_REQUEST, message)
    else -> ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error: ${message ?: javaClass.simpleName}")
}
