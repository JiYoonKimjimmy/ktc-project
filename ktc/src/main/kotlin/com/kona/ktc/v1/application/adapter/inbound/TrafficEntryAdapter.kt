package com.kona.ktc.v1.application.adapter.inbound

import com.kona.ktc.v1.application.dto.request.TrafficEntryRequest
import com.kona.ktc.v1.application.dto.response.TrafficResponse
import com.kona.ktc.v1.application.mapper.TrafficResponseMapper
import com.kona.ktc.v1.domain.model.TrafficToken
import com.kona.ktc.v1.domain.port.inbound.TrafficEntryPort
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RequestMapping("/api/v1/traffic")
@RestController
class TrafficEntryAdapter(
    private val trafficEntryPort: TrafficEntryPort,
    private val trafficResponseMapper: TrafficResponseMapper
) {

    @PostMapping("/entry")
    suspend fun entry(@RequestBody request: TrafficEntryRequest): TrafficResponse {
        val token = TrafficToken(zoneId = request.zoneId, token = request.token)
        val waiting = trafficEntryPort.entry(token)
        return trafficResponseMapper.toResponse(token, waiting)
    }

}