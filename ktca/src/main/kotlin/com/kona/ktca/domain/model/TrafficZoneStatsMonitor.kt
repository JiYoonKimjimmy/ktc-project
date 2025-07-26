package com.kona.ktca.domain.model

import com.kona.ktca.dto.StatsType

data class TrafficZoneStatsMonitor(
    val zoneId: String,
    val zoneAlias: String,

    // TrafficZoneMonitor -> TrafficZoneStatsMonitor
    var threshold: Long = 0,
    var entryCountPerDay: Long = 0,
    var waitingCount: Long = 0,
    var estimatedClearTime: Long = 0,

    // TrafficZoneStatsMonitorEntity -> TrafficZoneStatsMonitor
    var statsDate: String? = null,
    var statsType: StatsType? = null,
    var maxThreshold: Long = 0,
    var totalEntryCount: Long = 0,
    var maxWaitingCount: Long = 0,
    var maxEstimatedClearTime: Long = 0,
) {

    companion object {
        fun of(zone: TrafficZoneMonitor): TrafficZoneStatsMonitor {
            return TrafficZoneStatsMonitor(
                zoneId = zone.zoneId,
                zoneAlias = zone.zoneAlias,
            ).apply {
                threshold = zone.threshold
                entryCountPerDay = zone.entryCount
                waitingCount = zone.waitingCount
                estimatedClearTime = zone.estimatedClearTime
            }
        }
    }
}