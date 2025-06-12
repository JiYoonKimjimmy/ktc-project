package com.kona.ktca.infrastructure.repository

import com.kona.ktca.infrastructure.repository.entity.TrafficZoneMonitorEntity
import com.kona.ktca.infrastructure.repository.jpa.TrafficZoneMonitorJpaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Repository

@Repository
class TrafficZoneMonitorRepositoryImpl(
    private val trafficZoneMonitorJpaRepository: TrafficZoneMonitorJpaRepository
) : TrafficZoneMonitorRepository {

    override suspend fun saveAll(entities: List<TrafficZoneMonitorEntity>): List<TrafficZoneMonitorEntity> = withContext(Dispatchers.IO) {
        trafficZoneMonitorJpaRepository.saveAll(entities)
    }
    
}