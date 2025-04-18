package com.kona.ktc.v0.application.traffic

import com.kona.common.infra.util.SnowflakeIdGenerator
import com.kona.ktc.v0.application.traffic.dto.TrafficWaitRequest
import com.kona.ktc.v0.application.traffic.dto.TrafficWaitResponse
import com.kona.ktc.v0.domain.model.TrafficToken
import com.kona.ktc.v0.domain.repository.TrafficRepository
import org.springframework.stereotype.Service

@Service
class TrafficWaitUseCase(
    private val trafficRedisScriptRepository: TrafficRepository
) {
    suspend fun execute(request: TrafficWaitRequest): TrafficWaitResponse {
        val token = TrafficToken(
            token = request.token ?: SnowflakeIdGenerator.generate(),
            zoneId = request.zoneId,
            clientIp = request.clientIp,
            clientAgent = request.clientAgent
        )

        val waiting = trafficRedisScriptRepository.controlTraffic(token)

        return TrafficWaitResponse(
            canEnter = waiting.estimatedTime == 0L,
            zoneId = token.zoneId,
            token = token.token,
            waiting = if (waiting.estimatedTime != 0L) waiting else null
        )
    }
} 