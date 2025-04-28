package com.kona.common.infrastructure.error.exception

import com.kona.common.infrastructure.error.ErrorCode

open class BaseException(
    val errorCode: ErrorCode,
    var detailMessage: String? = null
): RuntimeException()