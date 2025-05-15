package com.kona.ktca.v1.domain.model

data class TrafficZoneWaiting(
    val entryCount: Long = 0,
    val waitingCount: Long = 0,
    val estimatedClearTime: Long = 0,
)