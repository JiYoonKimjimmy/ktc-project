package com.kona.ktca.v1.domain.port.outbound

import com.kona.ktca.v1.domain.model.TrafficZone

interface TrafficZoneFindPort {

    suspend fun findAllTrafficZone(zoneId: String?): List<TrafficZone>

    suspend fun findAllTrafficZoneWaiting(zones: List<TrafficZone>): List<TrafficZone>

}