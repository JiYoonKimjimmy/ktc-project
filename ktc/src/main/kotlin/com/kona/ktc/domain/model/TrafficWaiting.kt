package com.kona.ktc.domain.model

data class TrafficWaiting(
    val result: Long,
    val number: Long,
    val estimatedTime: Long,
    val totalCount: Long,
    val pollingPeriod: Long = 3000L,
) {
    val canEnter: Boolean
        get() = this.result == 1L

    constructor(result: List<Long>) : this(
        result = result[0],
        number = result[1],
        estimatedTime = result[2],
        totalCount = result[3]
    )
}