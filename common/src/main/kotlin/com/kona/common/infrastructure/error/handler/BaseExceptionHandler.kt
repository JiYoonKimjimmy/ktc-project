package com.kona.common.infrastructure.error.handler

import com.kona.common.application.dto.ErrorResponse
import com.kona.common.infrastructure.error.ErrorCode
import com.kona.common.infrastructure.error.FeatureCode
import com.kona.common.infrastructure.error.exception.BaseException
import com.kona.common.infrastructure.error.exception.ResourceNotFoundException
import com.kona.common.infrastructure.error.exception.RestClientServiceException
import com.kona.common.infrastructure.error.exception.ServiceUnavailableException
import com.kona.common.infrastructure.util.EMPTY
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.client.HttpClientErrorException

@ControllerAdvice
class BaseExceptionHandler(
    private val featureCode: FeatureCode = FeatureCode.UNKNOWN
) {

    @ExceptionHandler(ServiceUnavailableException::class)
    protected fun handleServiceUnavailableException(e: BaseException): ResponseEntity<ErrorResponse> {
        return ErrorResponse.toResponseEntity(FeatureCode.FAULTY, e.errorCode)
    }

    @ExceptionHandler(ResourceNotFoundException::class)
    protected fun handleResourceNotFoundException(e: BaseException): ResponseEntity<ErrorResponse> {
        return ErrorResponse.toResponseEntity(featureCode, e.errorCode)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    protected fun handleValidationException(e: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val message = e.bindingResult.fieldErrors.joinToString(". ") { it.defaultMessage ?: EMPTY }
        return ErrorResponse.toResponseEntity(featureCode, ErrorCode.ARGUMENT_NOT_VALID_ERROR, message)
    }

    @ExceptionHandler(HttpClientErrorException::class)
    protected fun handleHttpClientErrorException(e: HttpClientErrorException): ResponseEntity<ErrorResponse> {
        return ErrorResponse.toResponseEntity(featureCode, ErrorCode.EXTERNAL_SERVICE_ERROR, e.cause?.message)
    }

    @ExceptionHandler(RestClientServiceException::class)
    protected fun handleRestClientServiceException(e: RestClientServiceException): ResponseEntity<ErrorResponse> {
        return try {
            ErrorResponse.toResponseEntity(e.detailMessage!!)
        } catch (e: Exception) {
            ErrorResponse.toResponseEntity(featureCode, ErrorCode.EXTERNAL_SERVICE_ERROR, e.cause?.message)
        }
    }

    @ExceptionHandler(BaseException::class)
    protected fun handleCustomException(e: BaseException): ResponseEntity<ErrorResponse> {
        return ErrorResponse.toResponseEntity(featureCode, e.errorCode, e.detailMessage)
    }

    @ExceptionHandler(Exception::class)
    protected fun exceptionHandler(e: Exception): ResponseEntity<ErrorResponse> {
        return ErrorResponse.toResponseEntity(featureCode, ErrorCode.UNKNOWN_ERROR, e.cause?.message)
    }

}
