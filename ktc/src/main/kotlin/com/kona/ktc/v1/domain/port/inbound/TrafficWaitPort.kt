package com.kona.ktc.v1.domain.port.inbound

import com.kona.ktc.v1.domain.model.Traffic
import com.kona.ktc.v1.domain.model.TrafficWaiting
import java.time.Instant

interface TrafficWaitPort {

    suspend fun wait(traffic: Traffic, now: Instant = Instant.now()): TrafficWaiting

} 