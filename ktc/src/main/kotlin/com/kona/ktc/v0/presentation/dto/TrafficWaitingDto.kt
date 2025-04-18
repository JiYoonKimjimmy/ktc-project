package com.kona.ktc.v0.presentation.dto

data class TrafficWaitingDto(
    val number: Long,
    val estimatedTime: Long,
    val totalCount: Long,
    val poolingPeriod: Long = 5L
)