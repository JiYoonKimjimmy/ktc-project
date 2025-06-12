package com.kona.ktca.domain.service

import com.kona.ktca.domain.model.TrafficZoneMonitor
import com.kona.ktca.domain.port.inbound.TrafficZoneMonitorReadPort
import com.kona.ktca.domain.port.outbound.TrafficZoneMonitorFindPort
import org.springframework.stereotype.Service

@Service
class TrafficZoneMonitorReadService(
    private val trafficZoneMonitorFindPort: TrafficZoneMonitorFindPort
) : TrafficZoneMonitorReadPort {

    override suspend fun findLatestMonitoring(zoneId: String?): List<TrafficZoneMonitor> {
        return trafficZoneMonitorFindPort.findAllLatestTrafficZoneMonitor(zoneId)
    }

}