package com.kona.ktca.infrastructure.repository

import com.kona.common.infrastructure.enumerate.TrafficZoneGroupStatus
import com.kona.ktca.domain.dto.TrafficZoneGroupDTO
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

    override suspend fun findByPredicate(dto: TrafficZoneGroupDTO): TrafficZoneGroup? {
        return entities.values.find { checkPredicate(dto, it) }?.toDomain()
    }

    override suspend fun findAllByStatus(status: TrafficZoneGroupStatus): List<TrafficZoneGroup> {
        return entities.values.filter { it.status == status }.map { it.toDomain() }
    }

    override suspend fun delete(groupId: String) {
        entities.remove(groupId)
    }

    private fun checkPredicate(dto: TrafficZoneGroupDTO, entity: TrafficZoneGroupEntity): Boolean {
        return (dto.groupId?.let { it == entity.id } ?: true)
                && (dto.name?.let { it == entity.name } ?: true)
                && (dto.status?.let { it == entity.status } ?: true)
    }

}
