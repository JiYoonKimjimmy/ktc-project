package com.kona.ktc.v1.application.dto.mapper

import com.kona.common.infrastructure.util.SnowflakeIdGenerator
import com.kona.ktc.v1.application.dto.request.TrafficEntryRequest
import com.kona.ktc.v1.application.dto.request.TrafficWaitRequest
import com.kona.ktc.v1.application.dto.response.TrafficControlResponse
import com.kona.ktc.v1.application.dto.response.TrafficControlResponse.TrafficWaitingResponse
import com.kona.ktc.v1.domain.model.Traffic
import com.kona.ktc.v1.domain.model.TrafficWaiting
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
        token: Traffic,
        waiting: TrafficWaiting,
        pollingPeriod: Long = 5L
    ): TrafficControlResponse {
        return TrafficControlResponse(
            canEnter = waiting.canEnter,
            zoneId = token.zoneId,
            token = token.token,
            waiting = waiting.let(::TrafficWaitingResponse)
        )
    }

}