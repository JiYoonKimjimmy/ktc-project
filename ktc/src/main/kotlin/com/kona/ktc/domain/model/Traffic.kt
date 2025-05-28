package com.kona.ktc.domain.model

data class Traffic(
    val zoneId: String,
    val token: String,
    val clientIP: String? = null,
    val clientAgent: String? = null,
    val waiting: TrafficWaiting? = null
) {
    fun applyWaiting(waiting: TrafficWaiting): Traffic {
        return copy(waiting = waiting)
    }
}