package com.kona.common.infrastructure.util

import com.kona.common.infrastructure.error.exception.BaseException
import org.slf4j.Logger
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