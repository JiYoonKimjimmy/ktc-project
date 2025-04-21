package com.kona.ktc.v1.application.dto.response

import com.kona.ktc.v1.domain.model.TrafficWaiting

data class TrafficWaitResponse(
    val number: Long,
    val estimatedTime: Long,
    val totalCount: Long,
    val poolingPeriod: Long
) {
    constructor(waiting: TrafficWaiting): this(
        number = waiting.number,
        estimatedTime = waiting.estimatedTime,
        totalCount = waiting.totalCount,
        poolingPeriod = waiting.poolingPeriod
    )
}