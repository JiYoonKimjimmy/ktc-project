package com.kona.ktc.v1.domain.port.outbound

import com.kona.ktc.v1.domain.model.TrafficToken
import com.kona.ktc.v1.domain.model.TrafficWaiting

interface TrafficControlPort {

    suspend fun controlTraffic(token: TrafficToken): TrafficWaiting

} 