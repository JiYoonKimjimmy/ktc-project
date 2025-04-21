package com.kona.ktc.v1.domain.port.inbound

import com.kona.ktc.v1.domain.model.TrafficToken
import com.kona.ktc.v1.domain.model.TrafficWaiting

interface TrafficWaitPort {

    suspend fun wait(token: TrafficToken): TrafficWaiting

} 