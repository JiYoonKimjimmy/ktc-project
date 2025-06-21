package com.kona.ktca.domain.port.inbound

import com.kona.ktca.domain.model.TrafficZoneMonitor

interface TrafficZoneMonitorFindPort {

    suspend fun findLatestMonitoring(zoneId: String?): List<TrafficZoneMonitor>

}