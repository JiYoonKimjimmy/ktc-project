package com.kona.ktca.domain.model

import com.kona.common.infrastructure.enumerate.TrafficZoneStatus
import java.time.LocalDateTime

data class TrafficZoneMonitor(
    val id: String? = null,
    val zoneId: String,
    val zoneAlias: String,
    val threshold: Long,
    val status: TrafficZoneStatus,
    val activationTime: LocalDateTime,
    val entryCount: Long,
    val waitingCount: Long,
    val estimatedClearTime: Long,
) {
    companion object {
        fun of(trafficZone: TrafficZone, waiting: TrafficZoneWaiting): TrafficZoneMonitor {
            return TrafficZoneMonitor(
                zoneId = trafficZone.zoneId,
                zoneAlias = trafficZone.zoneAlias,
                threshold = trafficZone.threshold,
                status = trafficZone.status,
                activationTime = trafficZone.activationTime,
                entryCount = waiting.entryCount,
                waitingCount = waiting.waitingCount,
                estimatedClearTime = waiting.estimatedClearTime
            )
        }
    }
}