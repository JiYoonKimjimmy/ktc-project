package com.kona.ktc.presentation.dto.request

import com.kona.common.infrastructure.enumerate.ClientAgent

data class TrafficWaitRequest(
    val zoneId: String,
    val token: String?,
    val clientIp: String,
    val clientAgent: ClientAgent
)