package com.kona.ktca.domain.port.inbound

import com.kona.ktca.domain.event.TrafficZoneMonitorSavedEvent
import com.kona.ktca.domain.model.TrafficZoneStatsMonitor
import com.kona.ktca.dto.StatsType
import java.time.LocalDateTime

interface TrafficZoneStatsMonitorPort {
    suspend fun statsMonitor(statsType: StatsType, zoneId: String, startDate: String, endDate: String): List<TrafficZoneStatsMonitor>

    suspend fun applySaveEvent(now: LocalDateTime, event: TrafficZoneMonitorSavedEvent)
}