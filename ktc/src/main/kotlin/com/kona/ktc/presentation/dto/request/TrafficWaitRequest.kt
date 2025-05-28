package com.kona.ktc.presentation.dto.request

data class TrafficWaitRequest(
    val zoneId: String,
    val token: String?,
    val clientIP: String,
    val clientAgent: String
)