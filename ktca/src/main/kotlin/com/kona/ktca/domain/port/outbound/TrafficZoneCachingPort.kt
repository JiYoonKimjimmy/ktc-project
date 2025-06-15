package com.kona.ktca.domain.port.outbound

interface TrafficZoneCachingPort {

    suspend fun clearAll(zoneIds: List<String>)

}