package com.kona.ktca.infrastructure.repository

import com.kona.common.infrastructure.enumerate.TrafficZoneStatus
import com.kona.ktca.domain.dto.PageableDTO
import com.kona.ktca.domain.dto.TrafficZoneDTO
import com.kona.ktca.domain.model.TrafficZone
import com.kona.ktca.domain.port.outbound.TrafficZoneRepository
import com.kona.ktca.infrastructure.repository.entity.TrafficZoneEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import java.util.concurrent.ConcurrentHashMap

class FakeTrafficZoneRepository : TrafficZoneRepository {
    private val entities = ConcurrentHashMap<String, TrafficZoneEntity>()

    override suspend fun save(zone: TrafficZone): TrafficZone {
        val entity = TrafficZoneEntity.of(domain = zone)
        entities[entity.id] = entity
        return entity.toDomain()
    }

    override suspend fun findByZoneId(zoneId: String): TrafficZone? {
        return entities[zoneId]?.toDomain()
    }

    override suspend fun findByZoneIdAndStatusNot(zoneId: String, status: TrafficZoneStatus): TrafficZone? {
        return entities.values.find { it.id == zoneId && it.status != status }?.toDomain()
    }

    override suspend fun findAllByPredicate(dto: TrafficZoneDTO): List<TrafficZone> {
        return entities.values.filter { checkPredicate(dto, it) }.map { it.toDomain() }
    }

    override suspend fun findPage(
        dto: TrafficZoneDTO,
        pageable: PageableDTO,
    ): Page<TrafficZone> {
        val filteredList = entities.values
            .filter { checkPredicate(dto, it) }
            .toList()

        val totalElements = filteredList.size.toLong()
        val start = pageable.number * pageable.size
        val end = minOf(start + pageable.size, totalElements.toInt())

        val content = if (start < totalElements) {
            filteredList.subList(start, end).map { it.toDomain() }
        } else {
            emptyList()
        }

        return PageImpl(content, pageable.toPageRequest(), totalElements)
    }

    private fun checkPredicate(dto: TrafficZoneDTO, entity: TrafficZoneEntity): Boolean {
        return (dto.zoneId?.let { it == entity.id } ?: true)
                && (dto.zoneAlias?.let { it == entity.alias } ?: true)
                && (dto.threshold?.let { it == entity.threshold } ?: true)
                && (dto.activationTime?.let { it == entity.activationTime } ?: true)
                && (dto.status?.let { it == entity.status } ?: true)
    }

}