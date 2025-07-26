package com.kona.ktca.infrastructure.repository

import com.kona.ktca.domain.dto.TrafficZoneStatsMonitorDTO
import com.kona.ktca.domain.model.TrafficZoneStatsMonitor
import com.kona.ktca.domain.port.outbound.TrafficZoneStatsMonitorRepository
import com.kona.ktca.infrastructure.repository.entity.TrafficZoneStatsMonitorEntity
import com.kona.ktca.infrastructure.repository.entity.TrafficZoneStatsMonitorId
import com.kona.ktca.infrastructure.repository.jpa.TrafficZoneStatsMonitorJpaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Repository

@Repository
class TrafficZoneStatsMonitorRepositoryImpl(
    private val trafficZoneStatsMonitorJpaRepository: TrafficZoneStatsMonitorJpaRepository
) : TrafficZoneStatsMonitorRepository {

    override suspend fun findAllByIdIn(ids: List<TrafficZoneStatsMonitorId>): List<TrafficZoneStatsMonitor> = withContext(Dispatchers.IO) {
        trafficZoneStatsMonitorJpaRepository.findAllByIdIn(ids)
            .map { it.toDomain() }
    }

    override suspend fun saveAll(monitoring: List<TrafficZoneStatsMonitor>): List<TrafficZoneStatsMonitor> = withContext(Dispatchers.IO) {
        trafficZoneStatsMonitorJpaRepository.saveAll(
            monitoring.map { TrafficZoneStatsMonitorEntity.of(it) }
        ).map { it.toDomain() }
    }

    override suspend fun findAllByPredicate(dto: TrafficZoneStatsMonitorDTO): List<TrafficZoneStatsMonitor> = withContext(Dispatchers.IO) {
        val query = TrafficZoneStatsMonitorEntity.jpqlQuery(dto.toPredicatable())
        trafficZoneStatsMonitorJpaRepository.findAll { query() }.mapNotNull { it?.toDomain() }
    }
}