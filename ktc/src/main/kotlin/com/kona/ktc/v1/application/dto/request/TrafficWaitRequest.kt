package com.kona.ktc.v1.application.dto.request

import com.kona.common.enumerate.ClientAgent

data class TrafficWaitRequest(
    val zoneId: String,
    val token: String?,
    val clientIp: String,
    val clientAgent: ClientAgent
)