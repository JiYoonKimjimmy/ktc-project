package com.kona.ktc.v1.domain.port.outbound

import com.kona.ktc.v1.domain.model.Traffic
import com.kona.ktc.v1.domain.model.TrafficWaiting
import java.time.Instant

interface TrafficControlPort {

    suspend fun controlTraffic(traffic: Traffic, now: Instant = Instant.now()): TrafficWaiting

} 