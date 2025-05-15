package com.kona.ktc.v1.domain.port.inbound

import com.kona.ktc.v1.domain.model.TrafficToken
import com.kona.ktc.v1.domain.model.TrafficWaiting
import java.time.Instant

interface TrafficEntryPort {

    suspend fun entry(token: TrafficToken, now: Instant = Instant.now()): TrafficWaiting

} 