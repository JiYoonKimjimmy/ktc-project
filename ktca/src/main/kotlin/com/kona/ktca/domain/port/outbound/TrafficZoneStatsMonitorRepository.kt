package com.kona.ktca.domain.port.outbound

import com.kona.ktca.domain.dto.TrafficZoneStatsMonitorDTO
import com.kona.ktca.domain.model.TrafficZoneStatsMonitor
import com.kona.ktca.infrastructure.repository.entity.TrafficZoneStatsMonitorId

interface TrafficZoneStatsMonitorRepository {

    suspend fun findAllByIdIn(ids: List<TrafficZoneStatsMonitorId>): List<TrafficZoneStatsMonitor>

    suspend fun saveAll(monitoring: List<TrafficZoneStatsMonitor>): List<TrafficZoneStatsMonitor>

    suspend fun findAllByPredicate(dto: TrafficZoneStatsMonitorDTO): List<TrafficZoneStatsMonitor>
}