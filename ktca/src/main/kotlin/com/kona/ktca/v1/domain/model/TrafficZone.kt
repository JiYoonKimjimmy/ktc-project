package com.kona.ktca.v1.domain.model

import com.kona.common.infrastructure.enumerate.TrafficZoneStatus
import java.time.LocalDateTime

data class TrafficZone(
    val zoneId: String,
    val name: String,
    val threshold: Long,
    val activationTime: LocalDateTime,
    val status: TrafficZoneStatus,
    val waiting: TrafficZoneWaiting,
)