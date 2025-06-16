package com.kona.common.infrastructure.error.exception

import com.kona.common.infrastructure.error.ErrorCode

class ResourceNotFoundException(errorCode: ErrorCode) : BaseException(errorCode)