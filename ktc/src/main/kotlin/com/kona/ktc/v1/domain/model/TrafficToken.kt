package com.kona.ktc.v1.domain.model

import com.kona.common.enum.ClientAgent

data class TrafficToken(
    val zoneId: String,
    val token: String,
    val clientIp: String? = null,
    val clientAgent: ClientAgent? = null
)