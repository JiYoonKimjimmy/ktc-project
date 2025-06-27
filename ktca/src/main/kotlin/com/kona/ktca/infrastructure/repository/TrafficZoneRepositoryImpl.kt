package com.kona.ktca.infrastructure.repository

import com.kona.common.infrastructure.enumerate.TrafficZoneStatus
import com.kona.ktca.domain.dto.PageableDTO
import com.kona.ktca.domain.dto.TrafficZoneDTO
import com.kona.ktca.domain.model.TrafficZone
import com.kona.ktca.domain.port.outbound.TrafficZoneRepository
import com.kona.ktca.infrastructure.repository.entity.TrafficZoneEntity
import com.kona.ktca.infrastructure.repository.jpa.TrafficZoneJpaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.data.domain.Page
import org.springframework.stereotype.Repository

@Repository
class TrafficZoneRepositoryImpl(
    private val trafficZoneJpaRepository: TrafficZoneJpaRepository
) : TrafficZoneRepository {

    override suspend fun save(zone: TrafficZone): TrafficZone = withContext(Dispatchers.IO) {
        trafficZoneJpaRepository.save(TrafficZoneEntity.of(zone)).toDomain()
    }

    override suspend fun findByZoneId(zoneId: String): TrafficZone? = withContext(Dispatchers.IO) {
        trafficZoneJpaRepository.findById(zoneId).orElse(null)?.toDomain()
    }

    override suspend fun findByZoneIdAndStatusNot(zoneId: String, status: TrafficZoneStatus): TrafficZone? = withContext(Dispatchers.IO) {
        trafficZoneJpaRepository.findByIdAndStatusNot(zoneId, status)?.toDomain()
    }

    override suspend fun findAllByPredicate(dto: TrafficZoneDTO): List<TrafficZone> {
        val query = TrafficZoneEntity.jpqlQuery(dto.toPredicatable())
        return trafficZoneJpaRepository.findAll { query() }.mapNotNull { it?.toDomain() }
    }

    override suspend fun findPage(dto: TrafficZoneDTO, pageable: PageableDTO): Page<TrafficZone> {
        return trafficZoneJpaRepository.findPage(pageable.toPageRequest()) {
            select(entity(TrafficZoneEntity::class))
                .from(entity(TrafficZoneEntity::class))
                .whereAnd(*dto.toPredicatable())
            }
            .map { it?.toDomain() }
    }

    override suspend fun deleteByZoneId(zoneId: String) {
        return trafficZoneJpaRepository.deleteById(zoneId)
    }

}