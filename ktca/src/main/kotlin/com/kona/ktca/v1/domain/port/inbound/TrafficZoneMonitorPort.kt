package com.kona.ktca.v1.domain.port.inbound

import com.kona.ktca.v1.domain.model.TrafficMonitoring

interface TrafficZoneMonitorPort {

    fun monitoring(zoneId: String): TrafficMonitoring

}