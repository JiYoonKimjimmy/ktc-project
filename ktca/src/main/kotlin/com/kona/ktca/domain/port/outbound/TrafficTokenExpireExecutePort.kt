package com.kona.ktca.domain.port.outbound

import java.time.Instant

interface TrafficTokenExpireExecutePort {

    suspend fun expireTrafficToken(now: Instant = Instant.now()): Long

}