package com.kona.ktc.domain.model

import com.kona.common.infrastructure.error.ErrorCode
import com.kona.common.infrastructure.error.exception.InternalServiceException
import com.kona.common.infrastructure.error.exception.ServiceUnavailableException
import com.kona.common.infrastructure.util.DEFAULT_POLLING_PERIOD

data class TrafficWaiting(
    val result: Long,
    val number: Long,
    val estimatedTime: Long,
    val totalCount: Long,
) {
    val canEnter: Boolean
        get() = this.result == 1L
    val pollingPeriod: Long
        get() = calProgressiveEstimatedWaitTime(number)

    constructor(result: List<Long>) : this(
        result = result[0],
        number = result[1],
        estimatedTime = result[2],
        totalCount = result[3]
    )

    private fun calProgressiveEstimatedWaitTime(numberInQueue: Long): Long {
        return when (numberInQueue) {
            in 20_001..150_000 -> DEFAULT_POLLING_PERIOD * 2
            in 150_001..Long.MAX_VALUE -> DEFAULT_POLLING_PERIOD * 3
            else -> DEFAULT_POLLING_PERIOD
        }
    }

    fun validateTrafficWaitingResult(): TrafficWaiting {
        return when (result) {
            -100L -> throw InternalServiceException(ErrorCode.TRAFFIC_ZONE_NOT_FOUND)
            -102L -> throw InternalServiceException(ErrorCode.TRAFFIC_ZONE_STATUS_IS_BLOCKED)
            -503L -> throw ServiceUnavailableException(ErrorCode.FAULTY_503_ERROR)
            else -> this
        }
    }

}