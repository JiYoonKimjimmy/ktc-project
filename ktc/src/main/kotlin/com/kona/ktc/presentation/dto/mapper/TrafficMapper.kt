package com.kona.ktc.presentation.dto.mapper

import com.kona.common.infrastructure.util.SnowflakeIdGenerator
import com.kona.ktc.presentation.dto.request.TrafficEntryRequest
import com.kona.ktc.presentation.dto.request.TrafficWaitRequest
import com.kona.ktc.presentation.dto.response.TrafficControlResponse
import com.kona.ktc.presentation.dto.response.TrafficControlResponse.TrafficWaitingResponse
import com.kona.ktc.domain.model.Traffic
import com.kona.ktc.domain.model.TrafficWaiting
import org.springframework.stereotype.Component

@Component
class TrafficMapper {

    fun toDomain(request: TrafficWaitRequest): Traffic {
        return Traffic(
            zoneId = request.zoneId,
            token = request.token ?: SnowflakeIdGenerator.generate(),
            clientIp = request.clientIp,
            clientAgent = request.clientAgent
        )
    }

    fun toDomain(request: TrafficEntryRequest): Traffic {
        return Traffic(
            zoneId = request.zoneId,
            token = request.token
        )
    }

    fun toResponse(
        traffic: Traffic,
        waiting: TrafficWaiting
    ): TrafficControlResponse {
        return TrafficControlResponse(
            canEnter = waiting.canEnter,
            zoneId = traffic.zoneId,
            token = traffic.token,
            waiting = waiting.let(::TrafficWaitingResponse)
        )
    }

    fun toResponse(
        traffic: Traffic
    ): TrafficControlResponse {
        return TrafficControlResponse(
            canEnter = traffic.waiting?.canEnter ?: false,
            zoneId = traffic.zoneId,
            token = traffic.token,
            waiting = traffic.waiting?.let(::TrafficWaitingResponse)
        )
    }

}