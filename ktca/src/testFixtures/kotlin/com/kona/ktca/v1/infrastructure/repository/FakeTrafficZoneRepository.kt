package com.kona.ktca.v1.infrastructure.repository

import com.kona.common.infrastructure.enumerate.TrafficZoneStatus
import com.kona.ktca.v1.infrastructure.repository.entity.TrafficZoneEntity
import java.util.concurrent.ConcurrentHashMap

class FakeTrafficZoneRepository : TrafficZoneRepository {
    private val entities = ConcurrentHashMap<String, TrafficZoneEntity>()

    override suspend fun save(entity: TrafficZoneEntity): TrafficZoneEntity {
        entities[entity.id] = entity
        return entity
    }

    override suspend fun findByZoneId(zoneId: String): TrafficZoneEntity? {
        return entities[zoneId]
    }

    override suspend fun findAllByStatus(status: TrafficZoneStatus): List<TrafficZoneEntity> {
        return entities.values.filter { it.status == status }
    }
}