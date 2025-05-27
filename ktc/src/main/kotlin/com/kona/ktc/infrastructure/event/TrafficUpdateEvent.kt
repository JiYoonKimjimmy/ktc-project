package com.kona.ktc.infrastructure.event

import java.time.LocalDateTime

/**
 * DTO for SSE traffic update events
 */
data class TrafficUpdateEvent(
    val eventId: String,
    val eventType: TrafficEventType,
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val message: String,
    val data: Map<String, Any> = emptyMap()
)

/**
 * Types of traffic events that can be sent via SSE
 */
enum class TrafficEventType {
    TRAFFIC_WAIT,
    TRAFFIC_ENTRY,
    TOKEN_ISSUED,
    TOKEN_CONSUMED,
    TRAFFIC_ALERT,
    SYSTEM_NOTIFICATION
}
