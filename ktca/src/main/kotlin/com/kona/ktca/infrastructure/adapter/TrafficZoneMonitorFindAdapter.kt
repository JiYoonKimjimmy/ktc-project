package com.kona.ktca.infrastructure.adapter

import com.kona.common.infrastructure.enumerate.TrafficZoneStatus.ACTIVE
import com.kona.common.infrastructure.error.ErrorCode
import com.kona.common.infrastructure.error.exception.ResourceNotFoundException
import com.kona.ktca.domain.model.TrafficZone
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
        val zones = if (zoneId != null) {
            listOf(findTrafficZone(zoneId))
        } else {
            findAllTrafficZone()
        }
        val zonesMap = zones.associateBy { it.zoneId }
        val monitorsMap = findLatestTrafficZoneMonitoring().associateBy { it.zoneId }
        return zones.mapNotNull {
            if (monitorsMap[it.zoneId] != null && zonesMap[it.zoneId] != null) {
                monitorsMap[it.zoneId]!!.update(zonesMap[it.zoneId]!!)
            } else {
                null
            }
        }
    }

    private suspend fun findTrafficZone(zoneId: String): TrafficZone {
        return trafficZoneRepository.findByZoneId(zoneId)?.toDomain() ?: throw ResourceNotFoundException(ErrorCode.TRAFFIC_ZONE_NOT_FOUND)
    }

    private suspend fun findAllTrafficZone(): List<TrafficZone> {
        return trafficZoneRepository.findAllByStatus(ACTIVE).map { it.toDomain() }
    }

    private suspend fun findLatestTrafficZoneMonitoring(): List<TrafficZoneMonitor> {
        return trafficZoneMonitorCacheAdapter.findLatestTrafficZoneMonitoring()
    }

}