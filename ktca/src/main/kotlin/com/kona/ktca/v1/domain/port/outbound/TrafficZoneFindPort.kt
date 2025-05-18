package com.kona.ktca.v1.domain.port.outbound

import com.kona.ktca.v1.domain.model.TrafficZone
import com.kona.ktca.v1.domain.model.TrafficZoneWaiting

interface TrafficZoneFindPort {

    suspend fun findAllTrafficZone(zoneId: String?): List<TrafficZone>

    suspend fun findTrafficZoneWaiting(zoneId: String, threshold: Long): TrafficZoneWaiting

}