package com.kona.common.infrastructure.exception

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class GlobalExceptionHandler {

    companion object {
        private val log = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)
    }

    @ExceptionHandler(ErrorException::class)
    fun handleErrorException(e: ErrorException): ResponseEntity<Any> {
        log.error("Error occurred", e)
        val sampleErrorCode = e.sampleErrorCode

        val body = LinkedHashMap<String, Any>()
        body["status"] = sampleErrorCode.statusCode
        body["errorCode"] = sampleErrorCode.errorCode
        body["errorMessage"] = sampleErrorCode.errorMessage + e.additionalMessage

        return ResponseEntity(body, HttpStatus.valueOf(sampleErrorCode.statusCode))
    }

    @ExceptionHandler(Exception::class)
    fun handleGeneralException(e: Exception): ResponseEntity<Any> {
        log.error("Unhandled exception occurred", e)

        val body = LinkedHashMap<String, Any>()
        body["status"] = 400
        body["errorCode"] = "COMMON_400_001"
        body["errorMessage"] = "An unexpected error occurred: ${e.message ?: "No message"}"

        return ResponseEntity(body, HttpStatus.BAD_REQUEST)
    }
}
