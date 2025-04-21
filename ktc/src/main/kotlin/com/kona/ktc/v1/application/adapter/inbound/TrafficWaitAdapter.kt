package com.kona.ktc.v1.application.adapter.inbound

import com.kona.common.infra.util.SnowflakeIdGenerator
import com.kona.ktc.v1.application.dto.request.TrafficWaitRequest
import com.kona.ktc.v1.application.dto.response.TrafficResponse
import com.kona.ktc.v1.application.mapper.TrafficResponseMapper
import com.kona.ktc.v1.domain.model.TrafficToken
import com.kona.ktc.v1.domain.port.inbound.TrafficWaitPort
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RequestMapping("/api/v1/traffic")
@RestController
class TrafficWaitAdapter(
    private val trafficWaitPort: TrafficWaitPort,
    private val trafficResponseMapper: TrafficResponseMapper
) {

    @PostMapping("/wait")
    suspend fun wait(@RequestBody request: TrafficWaitRequest): TrafficResponse {
        val token = TrafficToken(
            zoneId = request.zoneId,
            token = request.token ?: SnowflakeIdGenerator.generate(),
            clientIp = request.clientIp,
            clientAgent = request.clientAgent
        )
        val waiting = trafficWaitPort.wait(token)
        return trafficResponseMapper.toResponse(token, waiting)
    }

} 