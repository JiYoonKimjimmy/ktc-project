package com.kona.common.infrastructure.util

import com.kona.common.infrastructure.error.exception.BaseException
import org.slf4j.Logger

fun Logger.error(exception: Exception): Exception {
    when (exception) {
        is BaseException -> this.error("${exception.errorCode.message}. ${exception.detailMessage}", exception)
        else -> this.error(exception.message, exception)
    }
    return exception
}