package com.kona.ktca.v1.domain.port.inbound

interface TrafficExpirePort {

    suspend fun expireTraffic()

}