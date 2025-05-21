package com.kona.ktca.domain.port.outbound

import com.kona.ktca.domain.model.TrafficZoneWaiting

interface TrafficZoneWaitingFindPort {

    suspend fun findTrafficZoneWaiting(zoneId: String, threshold: Long): TrafficZoneWaiting

}