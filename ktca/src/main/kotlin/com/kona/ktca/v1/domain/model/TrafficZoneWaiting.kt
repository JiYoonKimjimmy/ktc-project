package com.kona.ktca.v1.domain.model

data class TrafficZoneWaiting(
    val waitingCount: Long = 0,
    val entryCount: Long = 0,
    val estimatedClearTime: Long = 0,
)