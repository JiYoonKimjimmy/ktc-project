package com.kona.ktc.domain.model

import com.kona.common.infrastructure.enumerate.ClientAgent

data class Traffic(
    val zoneId: String,
    val token: String,
    val clientIp: String? = null,
    val clientAgent: ClientAgent? = null,
    val waiting: TrafficWaiting? = null
) {
    fun applyWaiting(waiting: TrafficWaiting): Traffic {
        return copy(waiting = waiting)
    }
}