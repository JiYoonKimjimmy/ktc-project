package com.kona.ktca.v1.infrastructure.repository

import com.kona.common.infrastructure.enumerate.TrafficZoneStatus
import com.kona.ktca.v1.infrastructure.repository.entity.TrafficZoneEntity
import com.kona.ktca.v1.infrastructure.repository.jpa.TrafficZoneJpaRepository
import com.linecorp.kotlinjdsl.querymodel.jpql.predicate.Predicatable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository

@Repository
class TrafficZoneRepositoryImpl(
    private val trafficZoneJpaRepository: TrafficZoneJpaRepository
) : TrafficZoneRepository {

    override suspend fun save(entity: TrafficZoneEntity): TrafficZoneEntity = withContext(Dispatchers.IO) {
        trafficZoneJpaRepository.save(entity)
    }

    override suspend fun findByZoneId(zoneId: String): TrafficZoneEntity? = withContext(Dispatchers.IO) {
        trafficZoneJpaRepository.findById(zoneId).orElse(null)
    }

    override suspend fun findAllByStatus(status: TrafficZoneStatus): List<TrafficZoneEntity> = withContext(Dispatchers.IO) {
        trafficZoneJpaRepository.findAllByStatus(status)
    }

    override suspend fun findPage(where: Array<Predicatable?>, pageable: Pageable): Page<TrafficZoneEntity?> {
        return trafficZoneJpaRepository.findPage(pageable) {
            select(entity(TrafficZoneEntity::class))
                .from(entity(TrafficZoneEntity::class))
                .whereAnd(*where)
        }
    }

}