package com.kona.ktc.v1.domain.model

data class TrafficWaiting(
    val canEnter: Boolean,
    val number: Long,
    val estimatedTime: Long,
    val totalCount: Long,
    val pollingPeriod: Long = 3000L,
) {
    constructor(canEnter: Long, number: Long, estimatedTime: Long, totalCount: Long) : this(
        canEnter = canEnter == 1L,
        number = number,
        estimatedTime = estimatedTime,
        totalCount = totalCount
    )

    constructor(result: List<Any>) : this(
        canEnter = (result[0] as Long) == 1L,
        number = result[1] as Long,
        estimatedTime = result[2] as Long,
        totalCount = result[3] as Long
    )
}