package com.kona.ktca.domain.port.outbound

import com.kona.ktca.domain.model.TrafficZoneMonitor

interface TrafficZoneMonitorFindPort {

    suspend fun findAllLatestTrafficZoneMonitor(zoneId: String?): List<TrafficZoneMonitor>

}