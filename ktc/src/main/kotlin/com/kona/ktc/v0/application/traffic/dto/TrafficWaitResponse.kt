package com.kona.ktc.v0.application.traffic.dto

import com.kona.ktc.v0.domain.model.TrafficWaiting

data class TrafficWaitResponse(
    val canEnter: Boolean,
    val zoneId: String,
    val token: String,
    val waiting: TrafficWaiting? = null
) 