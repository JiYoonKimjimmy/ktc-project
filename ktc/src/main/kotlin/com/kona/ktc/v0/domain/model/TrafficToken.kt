package com.kona.ktc.v0.domain.model

data class TrafficToken(
    val token: String,
    val zoneId: String,
    val clientIp: String? = null,
    val clientAgent: String? = null
) 