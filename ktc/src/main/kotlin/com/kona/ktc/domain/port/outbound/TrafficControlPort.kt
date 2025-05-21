package com.kona.ktc.domain.port.outbound

import com.kona.ktc.domain.model.Traffic
import com.kona.ktc.domain.model.TrafficWaiting
import java.time.Instant

interface TrafficControlPort {

    suspend fun controlTraffic(traffic: Traffic, now: Instant = Instant.now()): TrafficWaiting

} 