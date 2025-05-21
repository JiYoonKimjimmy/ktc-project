package com.kona.ktca.v1.domain.port.inbound

import com.kona.ktca.v1.domain.model.TrafficZone

interface TrafficZoneReadPort {

    suspend fun findTrafficZone(zoneId: String): TrafficZone

    suspend fun findTrafficZones(zoneId: String?, includeWaiting: Boolean = false): List<TrafficZone>

}