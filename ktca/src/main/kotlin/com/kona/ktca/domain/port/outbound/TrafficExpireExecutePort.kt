package com.kona.ktca.domain.port.outbound

import java.time.Instant

interface TrafficExpireExecutePort {

    suspend fun expireTraffic(now: Instant = Instant.now()): Long

}