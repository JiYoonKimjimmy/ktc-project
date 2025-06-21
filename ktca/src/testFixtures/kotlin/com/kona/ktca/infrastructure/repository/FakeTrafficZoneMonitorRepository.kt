package com.kona.ktca.infrastructure.repository

import com.kona.ktca.domain.model.TrafficZoneMonitor
import com.kona.ktca.domain.port.outbound.TrafficZoneMonitorRepository
import com.kona.ktca.infrastructure.repository.entity.TrafficZoneMonitorEntity
import java.util.concurrent.ConcurrentHashMap

class FakeTrafficZoneMonitorRepository : TrafficZoneMonitorRepository {
    private val entities = ConcurrentHashMap<String, TrafficZoneMonitorEntity>()

    override suspend fun saveAll(monitoring: List<TrafficZoneMonitor>): List<TrafficZoneMonitor> {
        val entities = monitoring.map { TrafficZoneMonitorEntity.of(it) }
        entities.forEach { this.entities[it.id] = it }
        return entities.map { it.toDomain() }
    }

    fun findAll(zoneId: String? = null): List<TrafficZoneMonitor> {
        return entities.values
            .filter { if (zoneId != null) it.zoneId == zoneId else true }
            .map { it.toDomain() }
    }

}