package com.kona.ktc.v1.domain.port.outbound

import com.kona.ktc.v1.domain.model.TrafficToken
import com.kona.ktc.v1.domain.model.TrafficWaiting
import java.time.Instant

interface TrafficControlPort {

    suspend fun controlTraffic(trafficToken: TrafficToken, now: Instant = Instant.now()): TrafficWaiting

} 