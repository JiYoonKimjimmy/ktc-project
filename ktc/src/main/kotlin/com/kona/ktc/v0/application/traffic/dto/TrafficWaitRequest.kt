package com.kona.ktc.v0.application.traffic.dto

data class TrafficWaitRequest(
    val zoneId: String,
    val token: String? = null,
    val clientIp: String? = null,
    val clientAgent: String? = null
) 