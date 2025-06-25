package com.kona.ktca.infrastructure.repository

import com.kona.common.infrastructure.enumerate.TrafficZoneGroupStatus
import com.kona.ktca.domain.model.TrafficZoneGroup
import com.kona.ktca.domain.port.outbound.TrafficZoneGroupRepository
import com.kona.ktca.infrastructure.repository.entity.TrafficZoneGroupEntity
import com.kona.ktca.infrastructure.repository.jpa.TrafficZoneGroupJpaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Repository
import kotlin.jvm.optionals.getOrNull

@Repository
class TrafficZoneGroupRepositoryImpl(
    private val trafficZoneGroupJpaRepository: TrafficZoneGroupJpaRepository
) : TrafficZoneGroupRepository {

    override suspend fun save(group: TrafficZoneGroup): TrafficZoneGroup = withContext(Dispatchers.IO) {
        trafficZoneGroupJpaRepository.save(TrafficZoneGroupEntity.of(domain = group)).toDomain()
    }

    override suspend fun saveNextOrder(name: String) = withContext(Dispatchers.IO) {
        val groupOrder = maxGroupOrder() + 1
        val entity = TrafficZoneGroupEntity.create(name, groupOrder)
        trafficZoneGroupJpaRepository.save(entity).toDomain()
    }

    override suspend fun findByGroupId(groupId: Long): TrafficZoneGroup? {
        return trafficZoneGroupJpaRepository.findById(groupId).getOrNull()?.toDomain()
    }

    override suspend fun findByGroupIdAndStatus(groupId: Long, status: TrafficZoneGroupStatus): TrafficZoneGroup? = withContext(Dispatchers.IO) {
        trafficZoneGroupJpaRepository.findAll(offset = 0, limit = 1) {
            select(entity(TrafficZoneGroupEntity::class))
                .from(entity(TrafficZoneGroupEntity::class))
                .whereAnd(
                    path(TrafficZoneGroupEntity::id).eq(groupId),
                    path(TrafficZoneGroupEntity::status).eq(status)
                )
        }.firstOrNull()?.toDomain()
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

    override suspend fun delete(groupId: Long) = withContext(Dispatchers.IO) {
        trafficZoneGroupJpaRepository.deleteById(groupId)
    }

    private suspend fun maxGroupOrder(): Int {
        return trafficZoneGroupJpaRepository.findAll {
            select(max(TrafficZoneGroupEntity::groupOrder))
                .from(entity(TrafficZoneGroupEntity::class))
        }.firstOrNull() ?: 0
    }

}