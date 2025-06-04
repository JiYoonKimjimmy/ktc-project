package com.kona.ktc.domain.model

import com.kona.common.infrastructure.util.DEFAULT_POLLING_PERIOD
import com.kona.common.infrastructure.util.calProgressiveEstimatedWaitTime

data class TrafficWaiting(
    val result: Long,
    val number: Long,
    val estimatedTime: Long,
    val totalCount: Long,
    val pollingPeriod: Long = DEFAULT_POLLING_PERIOD,
) {
    val canEnter: Boolean
        get() = this.result == 1L

    constructor(result: List<Long>) : this(
        result = result[0],
        number = result[1],
        estimatedTime = result[2],
        totalCount = result[3],
        pollingPeriod = calProgressiveEstimatedWaitTime(result[1] as Long)
    )
}