package com.kona.ktca.infrastructure.repository

import com.kona.common.infrastructure.enumerate.TrafficZoneGroupStatus
import com.kona.ktca.domain.dto.TrafficZoneGroupDTO
import com.kona.ktca.domain.model.TrafficZoneGroup
import com.kona.ktca.domain.port.outbound.TrafficZoneGroupRepository
import com.kona.ktca.infrastructure.repository.entity.TrafficZoneGroupEntity
import com.kona.ktca.infrastructure.repository.jpa.TrafficZoneGroupJpaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Repository

@Repository
class TrafficZoneGroupRepositoryImpl(
    private val trafficZoneGroupJpaRepository: TrafficZoneGroupJpaRepository
) : TrafficZoneGroupRepository {

    override suspend fun save(group: TrafficZoneGroup): TrafficZoneGroup = withContext(Dispatchers.IO) {
        trafficZoneGroupJpaRepository.save(TrafficZoneGroupEntity.of(domain = group)).toDomain()
    }

    override suspend fun saveNextOrder(group: TrafficZoneGroup) = withContext(Dispatchers.IO) {
        val nextOrder = maxGroupOrder() + 1
        val entity = TrafficZoneGroupEntity.of(domain = group, nextOrder = nextOrder)
        trafficZoneGroupJpaRepository.save(entity).toDomain()
    }

    override suspend fun findByPredicate(dto: TrafficZoneGroupDTO): TrafficZoneGroup? {
        val query = TrafficZoneGroupEntity.jpqlQuery(dto.toPredicatable())
        return trafficZoneGroupJpaRepository.findAll(0, 1) { query() }.firstOrNull()?.toDomain()
    }

    override suspend fun findAllByStatus(status: TrafficZoneGroupStatus): List<TrafficZoneGroup> = withContext(Dispatchers.IO) {
        trafficZoneGroupJpaRepository
            .findAll {
                select(entity(TrafficZoneGroupEntity::class))
                    .from(entity(TrafficZoneGroupEntity::class))
                    .where(path(TrafficZoneGroupEntity::status).eq(status))
            }
            .mapNotNull { it?.toDomain() }
    }

    override suspend fun delete(groupId: String) = withContext(Dispatchers.IO) {
        trafficZoneGroupJpaRepository.deleteById(groupId)
    }

    private suspend fun maxGroupOrder(): Int {
        return trafficZoneGroupJpaRepository.findAll {
            select(max(TrafficZoneGroupEntity::groupOrder))
                .from(entity(TrafficZoneGroupEntity::class))
        }.firstOrNull() ?: 0
    }

}