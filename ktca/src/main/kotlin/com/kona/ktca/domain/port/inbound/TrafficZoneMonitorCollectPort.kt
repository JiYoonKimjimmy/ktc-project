package com.kona.ktca.domain.port.inbound

import com.kona.ktca.domain.model.TrafficZoneMonitor

interface TrafficZoneMonitorCollectPort {

    suspend fun collect(zoneId: String? = null): List<TrafficZoneMonitor>

}