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
    val created: LocalDateTime? = null,
    val updated: LocalDateTime? = null,
) {
    companion object {
        fun of(zone: TrafficZone): TrafficZoneMonitor {
            return TrafficZoneMonitor(
                zoneId = zone.zoneId,
                zoneAlias = zone.zoneAlias,
                threshold = zone.threshold,
                status = zone.status,
                activationTime = zone.activationTime,
                entryCount = zone.waiting.entryCount,
                waitingCount = zone.waiting.waitingCount,
                estimatedClearTime = zone.waiting.estimatedClearTime
            )
        }
    }

    fun update(zone: TrafficZone): TrafficZoneMonitor {
        return copy(
            zoneAlias = zone.zoneAlias,
            threshold = zone.threshold,
            status = zone.status,
            activationTime = zone.activationTime,
            created = zone.created,
            updated = zone.updated
        )
    }

    fun isZeroWaitingCount(): Boolean {
        return waitingCount == 0L
    }

}