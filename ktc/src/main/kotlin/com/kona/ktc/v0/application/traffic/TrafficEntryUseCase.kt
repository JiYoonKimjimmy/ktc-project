package com.kona.ktc.v0.application.traffic

import com.kona.ktc.v0.application.traffic.dto.TrafficEntryRequest
import com.kona.ktc.v0.application.traffic.dto.TrafficEntryResponse
import com.kona.ktc.v0.domain.model.TrafficToken
import com.kona.ktc.v0.domain.repository.TrafficRepository
import org.springframework.stereotype.Service

@Service
class TrafficEntryUseCase(
    private val trafficRedisScriptRepository: TrafficRepository
) {
    suspend fun execute(request: TrafficEntryRequest): TrafficEntryResponse {
        val token = TrafficToken(
            token = request.token,
            zoneId = request.zoneId
        )

        val waiting = trafficRedisScriptRepository.controlTraffic(token)

        return TrafficEntryResponse(
            canEnter = waiting.estimatedTime == 0L,
            zoneId = token.zoneId,
            token = token.token,
            waiting = if (waiting.estimatedTime != 0L) waiting else null
        )
    }
} 