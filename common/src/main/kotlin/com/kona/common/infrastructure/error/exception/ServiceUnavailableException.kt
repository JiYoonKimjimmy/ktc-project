package com.kona.common.infrastructure.error.exception

import com.kona.common.infrastructure.error.ErrorCode

class ServiceUnavailableException(errorCode: ErrorCode) : BaseException(errorCode)