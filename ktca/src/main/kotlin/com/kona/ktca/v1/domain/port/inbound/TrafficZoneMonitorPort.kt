package com.kona.ktca.v1.domain.port.inbound

import com.kona.ktca.v1.domain.model.TrafficZone

interface TrafficZoneMonitorPort {

    suspend fun monitoring(zoneId: String? = null): List<TrafficZone>

}