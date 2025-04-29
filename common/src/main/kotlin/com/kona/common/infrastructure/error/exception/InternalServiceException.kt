package com.kona.common.infrastructure.error.exception

import com.kona.common.infrastructure.error.ErrorCode

class InternalServiceException(errorCode: ErrorCode) : BaseException(errorCode)