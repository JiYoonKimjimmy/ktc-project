package com.kona.ktca.domain.port.inbound

interface TrafficExpirePort {

    suspend fun expireTraffic()

}