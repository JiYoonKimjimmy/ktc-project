package com.kona.common.infrastructure.util

import com.kona.common.infrastructure.error.exception.BaseException
import org.slf4j.Logger
import org.slf4j.MDC
import java.time.LocalDateTime

fun Logger.error(exception: Exception): Exception {
    when (exception) {
        is BaseException -> this.error("${exception.errorCode.message}. ${exception.detailMessage}", exception)
        else -> this.error(exception.message, exception)
    }
    return exception
}

fun getCorrelationId(): String {
    val date = LocalDateTime.now().convertPatternOf(DATE_TIME_PATTERN_yyMMddHHmmss)
    val random = SnowflakeIdGenerator.generate(RADIX_HEX).takeLast(CORRELATION_ID_HEX_STRING_LENGTH)
    return "$date-$random"
}

fun setCorrelationId(correlationId: String?) {
    MDC.put(CORRELATION_ID_LOG_FIELD, correlationId)
}

fun String.getZoneId(): String {
    /**
     * [`zoneId` 정보 추출]
     * - "ktc:{<zoneId>}:zqueue" 형식의 key 문자열에서 `zoneId` 추출
     */
    return this.substringAfter(ZQUEUE_KEY_PREFIX).substringBefore(ZQUEUE_KEY_SUFFIX)
}

fun Long?.ifNullOrMinus(default: Long): Long {
    return if (this == null || this < 0) {
        default
    } else {
        this
    }
}