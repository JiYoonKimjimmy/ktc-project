package com.kona.ktc.infrastructure.error

import com.kona.common.application.dto.ErrorResponse
import com.kona.common.infrastructure.error.ErrorCode
import com.kona.common.infrastructure.error.FeatureCode
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.MediaType
import org.springframework.http.converter.HttpMessageNotWritableException
import org.springframework.web.HttpMediaTypeNotAcceptableException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.async.AsyncRequestTimeoutException
import java.io.IOException

@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
class SseExceptionHandler(
    private val featureCode: FeatureCode = FeatureCode.UNKNOWN
) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    @ExceptionHandler(IOException::class)
    fun handleIOException(e: IOException, request: HttpServletRequest): Any? {
        if (isEventStreamRequest(request)) {
            logger.info("SSE client disconnected: {}", request.remoteAddr)
            return null // 응답 없음, 조용히 처리
        } else {
            return ErrorResponse.toResponseEntity(featureCode, ErrorCode.UNKNOWN_ERROR, e.message)
        }
    }

    @ExceptionHandler(HttpMessageNotWritableException::class)
    fun handleMessageNotWritableException(
        e: HttpMessageNotWritableException,
        request: HttpServletRequest
    ): Any? {
        if (isEventStreamRequest(request)) {
            logger.info("Cannot write error message to SSE stream: {}", request.remoteAddr)
            return null
        } else {
            return ErrorResponse.toResponseEntity(featureCode, ErrorCode.UNKNOWN_ERROR, e.message)
        }
    }

    @ExceptionHandler(AsyncRequestTimeoutException::class)
    fun handleAsyncRequestTimeoutException(
        e: AsyncRequestTimeoutException,
        request: HttpServletRequest
    ): Any? {
        if (isEventStreamRequest(request)) {
            logger.info("SSE connection timed out: {}", request.remoteAddr)
            return null
        } else {
            return ErrorResponse.toResponseEntity(featureCode, ErrorCode.UNKNOWN_ERROR, e.message)
        }
    }

    @ExceptionHandler(HttpMediaTypeNotAcceptableException::class)
    fun handleMediaTypeNotAcceptableException(
        e: HttpMediaTypeNotAcceptableException,
        request: HttpServletRequest
    ): Any? {
        if (isEventStreamRequest(request)) {
            logger.info("Media type not acceptable for SSE: {}", request.remoteAddr)
            return null
        } else {
            return ErrorResponse.toResponseEntity(featureCode, ErrorCode.UNKNOWN_ERROR, e.message)
        }
    }

    private fun isEventStreamRequest(request: HttpServletRequest): Boolean {
        val acceptHeader = request.getHeader("Accept") ?: ""
        return acceptHeader.contains(MediaType.TEXT_EVENT_STREAM_VALUE)
    }
}
