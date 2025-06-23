package com.kona.common.infrastructure.error.handler

import com.kona.common.application.dto.ErrorResponse
import com.kona.common.infrastructure.error.ErrorCode
import com.kona.common.infrastructure.error.FeatureCode
import com.kona.common.infrastructure.error.exception.BaseException
import com.kona.common.infrastructure.error.exception.ResourceNotFoundException
import com.kona.common.infrastructure.error.exception.RestClientServiceException
import com.kona.common.infrastructure.error.exception.ServiceUnavailableException
import com.kona.common.infrastructure.util.EMPTY
import com.kona.common.infrastructure.util.error
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.client.HttpClientErrorException

@ControllerAdvice
class BaseExceptionHandler(
    private val featureCode: FeatureCode = FeatureCode.UNKNOWN
) {
    // logger
    private val logger = LoggerFactory.getLogger(this::class.java)

    private fun Exception.errorResponse(featureCode: FeatureCode, errorCode: ErrorCode, detailMessage: String? = null): ResponseEntity<ErrorResponse> {
        logger.error(this)
        return ErrorResponse.toResponseEntity(featureCode, errorCode, detailMessage ?: cause?.message ?: message)
    }

    private fun Exception.errorResponse(detailMessage: String): ResponseEntity<ErrorResponse> {
        logger.error(this)
        return ErrorResponse.toResponseEntity(detailMessage)
    }

    @ExceptionHandler(ServiceUnavailableException::class)
    fun handleServiceUnavailableException(exception: BaseException): ResponseEntity<ErrorResponse> {
        return exception.errorResponse(FeatureCode.FAULTY, exception.errorCode)
    }

    @ExceptionHandler(ResourceNotFoundException::class)
    fun handleResourceNotFoundException(exception: BaseException): ResponseEntity<ErrorResponse> {
        return exception.errorResponse(featureCode, exception.errorCode)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(exception: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val message = exception.bindingResult.fieldErrors.joinToString(". ") { it.defaultMessage ?: EMPTY }
        return exception.errorResponse(featureCode, ErrorCode.ARGUMENT_NOT_VALID_ERROR, message)
    }

    @ExceptionHandler(HttpClientErrorException::class)
    fun handleHttpClientErrorException(e: HttpClientErrorException): ResponseEntity<ErrorResponse> {
        return e.errorResponse(featureCode, ErrorCode.EXTERNAL_SERVICE_ERROR)
    }

    @ExceptionHandler(RestClientServiceException::class)
    fun handleRestClientServiceException(exception: RestClientServiceException): ResponseEntity<ErrorResponse> {
        return try {
            exception.errorResponse(exception.detailMessage!!)
        } catch (e: Exception) {
            e.errorResponse(featureCode, ErrorCode.EXTERNAL_SERVICE_ERROR)
        }
    }

    @ExceptionHandler(BaseException::class)
    fun handleCustomException(exception: BaseException): ResponseEntity<ErrorResponse> {
        return exception.errorResponse(featureCode, exception.errorCode, exception.detailMessage)
    }

    @ExceptionHandler(Exception::class)
    fun exceptionHandler(exception: Exception): ResponseEntity<ErrorResponse> {
        return exception.errorResponse(featureCode, ErrorCode.UNKNOWN_ERROR)
    }

}
