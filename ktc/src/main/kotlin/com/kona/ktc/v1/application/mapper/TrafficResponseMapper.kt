package com.kona.ktc.v1.application.mapper

import com.kona.ktc.v1.application.dto.response.TrafficResponse
import com.kona.ktc.v1.application.dto.response.TrafficWaitResponse
import com.kona.ktc.v1.domain.model.TrafficToken
import com.kona.ktc.v1.domain.model.TrafficWaiting
import org.springframework.stereotype.Component

@Component
class TrafficResponseMapper {

    fun toResponse(
        token: TrafficToken,
        waiting: TrafficWaiting?,
        poolingPeriod: Long = 5L
    ): TrafficResponse {
        return TrafficResponse(
            canEnter = waiting?.canEnter ?: false,
            zoneId = token.zoneId,
            token = token.token,
            waiting = waiting?.let(::TrafficWaitResponse)
        )
    }

}