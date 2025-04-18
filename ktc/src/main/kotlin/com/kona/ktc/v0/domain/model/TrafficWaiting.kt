package com.kona.ktc.v0.domain.model

data class TrafficWaiting(
    val number: Long,
    val estimatedTime: Long,
    val totalCount: Long,
    val poolingPeriod: Long = 5L
) 