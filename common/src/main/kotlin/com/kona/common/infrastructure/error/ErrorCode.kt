package com.kona.common.infrastructure.error

import org.springframework.http.HttpStatus

enum class ErrorCode(
    val status: HttpStatus,
    val code: String,
    val message: String
) {

    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "900", "Internal server error"),
    EXTERNAL_SERVICE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "901", "External API service error"),
    ARGUMENT_NOT_VALID_ERROR(HttpStatus.BAD_REQUEST, "902", "Argument not valid"),
    REDISSON_LOCK_ATTEMPT_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "903", "Redisson lock attempt error"),

    UNKNOWN_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "999", "Unknown error"),

}
