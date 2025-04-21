package com.kona.ktc.v1.domain.service

import com.kona.ktc.v1.domain.model.TrafficToken
import com.kona.ktc.v1.domain.model.TrafficWaiting
import com.kona.ktc.v1.domain.port.inbound.TrafficWaitPort
import com.kona.ktc.v1.domain.port.outbound.TrafficControlPort
import org.springframework.stereotype.Service

@Service
class TrafficWaitService(
    private val trafficRedisScriptAdapter: TrafficControlPort
) : TrafficWaitPort {

    override suspend fun wait(token: TrafficToken): TrafficWaiting {
        return trafficRedisScriptAdapter.controlTraffic(token)
    }

} 