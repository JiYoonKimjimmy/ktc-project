package com.kona.ktca.domain.service

import com.kona.common.infrastructure.enumerate.TrafficZoneStatus.ACTIVE
import com.kona.common.infrastructure.error.ErrorCode
import com.kona.common.infrastructure.error.exception.ResourceNotFoundException
import com.kona.ktca.domain.dto.TrafficZoneDTO
import com.kona.ktca.domain.model.TrafficZone
import com.kona.ktca.domain.model.TrafficZoneMonitor
import com.kona.ktca.domain.port.inbound.TrafficZoneMonitorFindPort
import com.kona.ktca.domain.port.outbound.TrafficZoneMonitorCachingPort
import com.kona.ktca.domain.port.outbound.TrafficZoneRepository
import org.springframework.stereotype.Service

@Service
class TrafficZoneMonitorFindService(
    private val trafficZoneRepository: TrafficZoneRepository,
    private val trafficZoneMonitorCachingPort: TrafficZoneMonitorCachingPort
) : TrafficZoneMonitorFindPort {

    override suspend fun findLatestMonitoring(zoneId: String?): List<TrafficZoneMonitor> {
        val zones = if (zoneId != null) {
            listOf(findTrafficZone(zoneId))
        } else {
            findAllTrafficZone()
        }
        val zonesMap = zones.associateBy { it.zoneId }
        val monitorsMap = findLatestTrafficZoneMonitoring().associateBy { it.zoneId }
        return zones.mapNotNull {
            monitorsMap[it.zoneId]?.updateTrafficZone(zone = zonesMap[it.zoneId])
        }
    }

    private suspend fun findTrafficZone(zoneId: String): TrafficZone {
        return trafficZoneRepository.findByZoneId(zoneId) ?: throw ResourceNotFoundException(ErrorCode.TRAFFIC_ZONE_NOT_FOUND)
    }

    private suspend fun findAllTrafficZone(): List<TrafficZone> {
        return trafficZoneRepository.findAllByPredicate(dto = TrafficZoneDTO(status = ACTIVE))
    }

    private suspend fun findLatestTrafficZoneMonitoring(): List<TrafficZoneMonitor> {
        return trafficZoneMonitorCachingPort.findAllMonitoringLatestResult()
    }

    private suspend fun TrafficZoneMonitor.updateTrafficZone(zone: TrafficZone?): TrafficZoneMonitor? {
        return if (zone != null) {
            this.update(zone)
        } else {
            null
        }
    }

}