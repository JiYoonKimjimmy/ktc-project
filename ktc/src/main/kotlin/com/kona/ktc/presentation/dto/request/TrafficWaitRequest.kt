package com.kona.ktc.presentation.dto.request

import com.kona.common.infrastructure.error.ErrorCode
import com.kona.common.infrastructure.error.exception.RequestParameterMissingException

data class  TrafficWaitRequest(
    val zoneId: String,
    val token: String?,
    val clientIP: String?,
    val clientAgent: String
) {
    fun validate(): TrafficWaitRequest {
        require(zoneId.isNotBlank()) { throw RequestParameterMissingException(ErrorCode.REQUIRED_REQUEST_PARAMETER_MISSING_ERROR, "zoneId") }
        require(clientAgent.isNotBlank()) { throw RequestParameterMissingException(ErrorCode.REQUIRED_REQUEST_PARAMETER_MISSING_ERROR, "clientAgent") }
        return this
    }
}