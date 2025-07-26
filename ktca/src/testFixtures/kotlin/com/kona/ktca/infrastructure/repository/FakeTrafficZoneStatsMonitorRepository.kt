package com.kona.ktca.infrastructure.repository

import com.kona.ktca.domain.dto.TrafficZoneStatsMonitorDTO
import com.kona.ktca.domain.model.TrafficZoneStatsMonitor
import com.kona.ktca.domain.port.outbound.TrafficZoneStatsMonitorRepository
import com.kona.ktca.infrastructure.repository.entity.TrafficZoneStatsMonitorEntity
import com.kona.ktca.infrastructure.repository.entity.TrafficZoneStatsMonitorId
import java.util.concurrent.ConcurrentHashMap

class FakeTrafficZoneStatsMonitorRepository : TrafficZoneStatsMonitorRepository {
    private val entities = ConcurrentHashMap<TrafficZoneStatsMonitorId, TrafficZoneStatsMonitorEntity>()
    override suspend fun findAllByIdIn(ids: List<TrafficZoneStatsMonitorId>): List<TrafficZoneStatsMonitor> {
        return entities.values
            .filter { ids.contains(it.id) }
            .map { it.toDomain() }
    }

    override suspend fun saveAll(monitoring: List<TrafficZoneStatsMonitor>): List<TrafficZoneStatsMonitor> {
        val newEntities = monitoring.map { TrafficZoneStatsMonitorEntity.of(it) }
        newEntities.forEach { entity ->
            entities[entity.id] = entity
        }
        return newEntities.map { it.toDomain() }
    }

    override suspend fun findAllByPredicate(dto: TrafficZoneStatsMonitorDTO): List<TrafficZoneStatsMonitor> {
        return entities.values.filter {
            var isMatch = true
            if (dto.zoneId != null) {
                isMatch = isMatch && it.id.zoneId == dto.zoneId
            }
            if (dto.statsType != null) {
                isMatch = isMatch && it.statsType == dto.statsType
            }
            val startDate = dto.startDate
            if (startDate != null) {
                isMatch = isMatch && it.id.statsDate >= startDate
            }
            val endDate = dto.endDate
            if (endDate != null) {
                isMatch = isMatch && it.id.statsDate <= endDate
            }
            isMatch
        }.map { it.toDomain() }.sortedBy { it.statsDate }
    }

    suspend fun getEntities(): ConcurrentHashMap<TrafficZoneStatsMonitorId, TrafficZoneStatsMonitorEntity> {
        return entities
    }

}