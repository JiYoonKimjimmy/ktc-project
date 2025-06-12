package com.kona.ktca.domain.port.inbound

interface TrafficTokenExpirePort {

    suspend fun expireTraffic(): Long

}