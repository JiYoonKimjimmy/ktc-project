package com.kona.ktc.v1.domain.model

import com.kona.common.infrastructure.enumerate.ClientAgent

data class Traffic(
    val zoneId: String,
    val token: String,
    val clientIp: String? = null,
    val clientAgent: ClientAgent? = null
)