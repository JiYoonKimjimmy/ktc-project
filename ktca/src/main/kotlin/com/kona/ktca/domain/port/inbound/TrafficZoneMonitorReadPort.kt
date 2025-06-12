package com.kona.ktca.domain.port.inbound

import com.kona.ktca.domain.model.TrafficZoneMonitor

interface TrafficZoneMonitorReadPort {

    suspend fun findLatestMonitoring(zoneId: String?): List<TrafficZoneMonitor>

}