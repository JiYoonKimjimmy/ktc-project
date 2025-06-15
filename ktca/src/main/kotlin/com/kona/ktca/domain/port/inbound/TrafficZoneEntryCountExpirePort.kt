package com.kona.ktca.domain.port.inbound

interface TrafficZoneEntryCountExpirePort {

    suspend fun expireTrafficZoneEntryCount(zoneIds: List<String>): Int

}