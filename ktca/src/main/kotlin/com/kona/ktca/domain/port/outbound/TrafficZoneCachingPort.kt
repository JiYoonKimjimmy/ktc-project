package com.kona.ktca.domain.port.outbound

import com.kona.ktca.domain.model.TrafficZone

interface TrafficZoneCachingPort {

    suspend fun save(zone: TrafficZone): TrafficZone

    suspend fun findTrafficZoneWaiting(zone: TrafficZone): TrafficZone

    suspend fun clearAll(zoneIds: List<String>)

}