package com.kona.ktca.v1.domain.model

import com.kona.common.infrastructure.enumerate.TrafficZoneStatus
import java.time.LocalDateTime

data class TrafficZone(
    val zoneId: String,
    val name: String,
    val threshold: Long,
    val activationTime: LocalDateTime,
    val status: TrafficZoneStatus,
    var waiting: TrafficZoneWaiting,
) {

    constructor(zoneId: String, threshold: Long) : this(
        zoneId = zoneId,
        name = zoneId,
        threshold = threshold,
        activationTime = LocalDateTime.now(),
        status = TrafficZoneStatus.ACTIVE,
        waiting = TrafficZoneWaiting(),
    )

    suspend fun applyWaiting(function: suspend (String, Long) -> TrafficZoneWaiting): TrafficZone {
        this.waiting = function(zoneId, threshold)
        return this
    }

}