package com.kona.ktc.v1.application.adapter.inbound

import com.kona.ktc.v1.application.dto.mapper.TrafficMapper
import com.kona.ktc.v1.application.dto.request.TrafficEntryRequest
import com.kona.ktc.v1.application.dto.request.TrafficWaitRequest
import com.kona.ktc.v1.application.dto.response.TrafficControlResponse
import com.kona.ktc.v1.domain.port.inbound.TrafficEntryPort
import com.kona.ktc.v1.domain.port.inbound.TrafficWaitPort
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RequestMapping("/api/v1/traffic")
@RestController
class TrafficControlAdapter(
    private val trafficMapper: TrafficMapper,
    private val trafficWaitPort: TrafficWaitPort,
    private val trafficEntryPort: TrafficEntryPort
) {

    @PostMapping("/wait")
    suspend fun wait(@RequestBody request: TrafficWaitRequest): ResponseEntity<TrafficControlResponse> {
        val traffic = trafficMapper.toDomain(request)
        val waiting = trafficWaitPort.wait(traffic)
        return trafficMapper.toResponse(traffic, waiting).success(HttpStatus.OK)
    }

    @PostMapping("/entry")
    suspend fun entry(@RequestBody request: TrafficEntryRequest): ResponseEntity<TrafficControlResponse> {
        val traffic = trafficMapper.toDomain(request)
        val waiting = trafficEntryPort.entry(traffic)
        return trafficMapper.toResponse(traffic, waiting).success(HttpStatus.OK)
    }

}