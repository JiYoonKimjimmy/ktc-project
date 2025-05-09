package com.kona.ktca.v1.domain.model

data class TrafficZoneWaiting(
    val waitingCount: Int = 0,
    val entryCount: Int = 0,
    val estimatedClearTime: Int = 0,
)