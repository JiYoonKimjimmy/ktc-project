package com.kona.ktc.v1.application.adapter.inbound

import com.kona.ktc.v1.application.dto.mapper.TrafficTokenMapper
import com.kona.ktc.v1.application.dto.request.TrafficWaitRequest
import com.kona.ktc.v1.application.dto.response.TrafficTokenResponse
import com.kona.ktc.v1.domain.port.inbound.TrafficWaitPort
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RequestMapping("/api/v1/traffic")
@RestController
class TrafficWaitAdapter(
    private val trafficWaitPort: TrafficWaitPort,
    private val trafficTokenMapper: TrafficTokenMapper
) {

    @PostMapping("/wait")
    suspend fun wait(@RequestBody request: TrafficWaitRequest): TrafficTokenResponse {
        val token = trafficTokenMapper.toDomain(request)
        val waiting = trafficWaitPort.wait(token)
        return trafficTokenMapper.toResponse(token, waiting)
    }

} 