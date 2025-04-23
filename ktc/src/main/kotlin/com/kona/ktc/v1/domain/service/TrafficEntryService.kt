package com.kona.ktc.v1.domain.service

import com.kona.ktc.v1.domain.model.TrafficToken
import com.kona.ktc.v1.domain.model.TrafficWaiting
import com.kona.ktc.v1.domain.port.inbound.TrafficEntryPort
import com.kona.ktc.v1.domain.port.outbound.TrafficControlPort
import org.springframework.stereotype.Service

@Service
class TrafficEntryService(
    private val trafficControlRedisAdapter: TrafficControlPort
) : TrafficEntryPort {

    override suspend fun entry(token: TrafficToken): TrafficWaiting {
        return trafficControlRedisAdapter.controlTraffic(token)
    }

}