package com.kona.ktca.domain.service

import com.kona.common.infrastructure.enumerate.TrafficZoneStatus.ACTIVE
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

    override suspend fun findLatestMonitoring(zoneId: String?, groupId: String?): List<TrafficZoneMonitor> {
        val zones = findAllTrafficZone(zoneId, groupId)
        val zonesMap = zones.associateBy { it.zoneId }
        val monitorsMap = findLatestTrafficZoneMonitoring().associateBy { it.zoneId }
        return zones.mapNotNull {
            monitorsMap[it.zoneId]?.updateTrafficZone(zone = zonesMap[it.zoneId])
        }
    }

    private suspend fun findAllTrafficZone(zoneId: String?, groupId: String?): List<TrafficZone> {
        return trafficZoneRepository.findAllByPredicate(dto = TrafficZoneDTO(zoneId = zoneId, groupId = groupId, status = ACTIVE))
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