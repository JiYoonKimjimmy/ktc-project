package com.kona.ktca.infrastructure.adapter

import com.kona.common.infrastructure.enumerate.TrafficZoneStatus.ACTIVE
import com.kona.ktca.domain.model.TrafficZoneMonitor
import com.kona.ktca.domain.port.outbound.TrafficZoneMonitorFindPort
import com.kona.ktca.infrastructure.cache.TrafficZoneMonitorCacheAdapter
import com.kona.ktca.infrastructure.repository.TrafficZoneRepository
import org.springframework.stereotype.Component

@Component
class TrafficZoneMonitorFindAdapter(
    private val trafficZoneRepository: TrafficZoneRepository,
    private val trafficZoneMonitorCacheAdapter: TrafficZoneMonitorCacheAdapter
) : TrafficZoneMonitorFindPort {

    override suspend fun findAllLatestTrafficZoneMonitor(zoneId: String?): List<TrafficZoneMonitor> {
        val zoneIds = if (zoneId != null) {
            listOf(zoneId)
        } else {
            trafficZoneRepository.findAllByStatus(ACTIVE).map { it.id }
        }
        return trafficZoneMonitorCacheAdapter.findLatestTrafficZoneMonitoring().filter { it.zoneId in zoneIds }
    }

}