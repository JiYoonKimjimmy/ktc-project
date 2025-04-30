package com.kona.ktca.v1.domain.port.outbound

interface TrafficExpirePort {

    suspend fun expireTraffic(): Long

}