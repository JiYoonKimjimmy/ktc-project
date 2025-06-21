package com.kona.ktca.domain.port.outbound

import com.kona.ktca.domain.model.TrafficZoneMonitor

interface TrafficZoneMonitorRepository {

    suspend fun saveAll(monitoring: List<TrafficZoneMonitor>): List<TrafficZoneMonitor>

}