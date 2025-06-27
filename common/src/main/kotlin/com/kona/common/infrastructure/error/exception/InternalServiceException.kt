package com.kona.common.infrastructure.error.exception

import com.kona.common.infrastructure.error.ErrorCode

class InternalServiceException(errorCode: ErrorCode) : BaseException(errorCode) {

    constructor(errorCode: ErrorCode, detailMessage: String?): this(errorCode) {
        this.detailMessage = detailMessage
    }

}