package com.kona.ktca.infrastructure.repository

import com.kona.common.infrastructure.enumerate.TrafficZoneGroupStatus
import com.kona.ktca.domain.model.TrafficZoneGroup
import com.kona.ktca.domain.port.outbound.TrafficZoneGroupRepository
import com.kona.ktca.infrastructure.repository.entity.TrafficZoneGroupEntity
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap

class FakeTrafficZoneGroupRepository : TrafficZoneGroupRepository {

    private val entities = ConcurrentHashMap<String, TrafficZoneGroupEntity>()

    override suspend fun save(group: TrafficZoneGroup): TrafficZoneGroup {
        val entity = TrafficZoneGroupEntity.of(domain = group)
        if (entity.created == null) {
            entity.created = LocalDateTime.now()
            entity.updated = LocalDateTime.now()
        } else {
            entity.updated = LocalDateTime.now()
        }
        entities[entity.id] = entity
        return entity.toDomain()
    }

    override suspend fun saveNextOrder(group: TrafficZoneGroup): TrafficZoneGroup {
        val groupOrder = (entities.values.maxOfOrNull { it.groupOrder } ?: 0) + 1
        return save(group.copy(order = groupOrder))
    }

    override suspend fun findByGroupId(groupId: String): TrafficZoneGroup? {
        return entities[groupId]?.toDomain()
    }

    override suspend fun findByGroupIdAndStatus(groupId: String, status: TrafficZoneGroupStatus): TrafficZoneGroup? {
        return entities[groupId]?.takeIf { it.status == status }?.toDomain()
    }

    override suspend fun findAllByStatus(status: TrafficZoneGroupStatus): List<TrafficZoneGroup> {
        return entities.values.filter { it.status == status }.map { it.toDomain() }
    }

    override suspend fun delete(groupId: String) {
        entities.remove(groupId)
    }

}
