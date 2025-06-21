package com.kona.ktca.infrastructure.repository

import com.kona.ktca.domain.model.TrafficZoneMonitor
import com.kona.ktca.domain.port.outbound.TrafficZoneMonitorRepository
import com.kona.ktca.infrastructure.repository.entity.TrafficZoneMonitorEntity
import com.kona.ktca.infrastructure.repository.jpa.TrafficZoneMonitorJpaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Repository

@Repository
class TrafficZoneMonitorRepositoryImpl(
    private val trafficZoneMonitorJpaRepository: TrafficZoneMonitorJpaRepository
) : TrafficZoneMonitorRepository {

    override suspend fun saveAll(monitoring: List<TrafficZoneMonitor>): List<TrafficZoneMonitor> = withContext(Dispatchers.IO) {
        monitoring.map { TrafficZoneMonitorEntity.of(it) }
            .let { trafficZoneMonitorJpaRepository.saveAll(it) }
            .map { it.toDomain() }
    }
    
}