package com.kona.ktca.infrastructure.adapter

import com.kona.ktca.domain.model.TrafficZoneMonitor
import com.kona.ktca.domain.port.outbound.TrafficZoneMonitorSavePort
import com.kona.ktca.infrastructure.repository.TrafficZoneMonitorRepository
import com.kona.ktca.infrastructure.repository.entity.TrafficZoneMonitorEntity
import org.springframework.stereotype.Component

@Component
class TrafficZoneMonitorSaveAdapter(
    private val trafficZoneMonitorRepository: TrafficZoneMonitorRepository
) : TrafficZoneMonitorSavePort {

    override suspend fun saveAll(monitoring: List<TrafficZoneMonitor>): List<TrafficZoneMonitor> {
        return monitoring
            .map { TrafficZoneMonitorEntity.of(domain = it) }
            .let { trafficZoneMonitorRepository.saveAll(entities = it) }
            .map { it.toDomain()}
    }

}