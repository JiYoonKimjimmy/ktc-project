package com.kona.ktc.v0.presentation.dto

data class TrafficWaitRequestDto(
    val zoneId: String,
    val token: String?,
    val clientIp: String,
    val clientAgent: String
) 