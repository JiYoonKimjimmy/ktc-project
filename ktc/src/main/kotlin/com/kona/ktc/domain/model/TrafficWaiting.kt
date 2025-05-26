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

    constructor(result: List<Any>) : this(
        result = result[0] as Long,
        number = result[1] as Long,
        estimatedTime = result[2] as Long,
        totalCount = result[3] as Long
    )
}