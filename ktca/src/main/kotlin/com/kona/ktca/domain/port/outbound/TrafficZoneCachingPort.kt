package com.kona.ktca.domain.port.outbound

interface TrafficZoneCachingPort {

    suspend fun clear(zoneIds: List<String>)

}