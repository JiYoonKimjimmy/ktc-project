package com.kona.ktc.presentation.dto.request

import com.kona.common.infrastructure.error.ErrorCode
import com.kona.common.infrastructure.error.exception.RequestParameterMissingException

data class TrafficEntryRequest(
    val zoneId: String,
    val token: String
) {
    fun validate(): TrafficEntryRequest {
        require(zoneId.isNotBlank()) { throw RequestParameterMissingException(ErrorCode.REQUIRED_REQUEST_PARAMETER_MISSING_ERROR, "zoneId") }
        return this
    }
}