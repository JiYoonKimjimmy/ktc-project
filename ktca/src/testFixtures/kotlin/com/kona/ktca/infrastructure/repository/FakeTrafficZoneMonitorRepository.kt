package com.kona.ktca.infrastructure.repository

import com.kona.ktca.infrastructure.repository.entity.TrafficZoneMonitorEntity
import java.util.concurrent.ConcurrentHashMap

class FakeTrafficZoneMonitorRepository : TrafficZoneMonitorRepository {
    private val entities = ConcurrentHashMap<String, TrafficZoneMonitorEntity>()

    override suspend fun saveAll(entities: List<TrafficZoneMonitorEntity>): List<TrafficZoneMonitorEntity> {
        entities.forEach { entity ->
            this.entities[entity.id] = entity
        }
        return entities
    }

    fun findAll(zoneId: String? = null): List<TrafficZoneMonitorEntity> {
        val result = entities.values.toList()

        return if (zoneId != null) {
            result.filter { it.zoneId == zoneId }
        } else {
            result
        }
    }

}