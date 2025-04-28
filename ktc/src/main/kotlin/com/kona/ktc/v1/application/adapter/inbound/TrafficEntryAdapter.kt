package com.kona.ktc.v1.application.adapter.inbound

import com.kona.ktc.v1.application.dto.mapper.TrafficTokenMapper
import com.kona.ktc.v1.application.dto.request.TrafficEntryRequest
import com.kona.ktc.v1.application.dto.response.TrafficTokenResponse
import com.kona.ktc.v1.domain.port.inbound.TrafficEntryPort
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RequestMapping("/api/v1/traffic")
@RestController
class TrafficEntryAdapter(
    private val trafficEntryPort: TrafficEntryPort,
    private val trafficTokenMapper: TrafficTokenMapper
) {

    @PostMapping("/entry")
    suspend fun entry(@RequestBody request: TrafficEntryRequest): TrafficTokenResponse {
        val token = trafficTokenMapper.toDomain(request)
        val waiting = trafficEntryPort.entry(token)
        return trafficTokenMapper.toResponse(token, waiting)
    }

}