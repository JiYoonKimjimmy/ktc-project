package com.kona.ktca.v1.domain.port.inbound

import com.kona.ktca.v1.domain.model.TrafficMonitoring

interface TrafficZoneMonitorPort {

    suspend fun monitoring(zoneId: String? = null): TrafficMonitoring

}