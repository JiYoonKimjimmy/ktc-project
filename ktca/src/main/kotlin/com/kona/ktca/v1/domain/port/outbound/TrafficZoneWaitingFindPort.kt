package com.kona.ktca.v1.domain.port.outbound

import com.kona.ktca.v1.domain.model.TrafficZoneWaiting

interface TrafficZoneWaitingFindPort {

    suspend fun findTrafficZoneWaiting(zoneId: String, threshold: Long): TrafficZoneWaiting

}