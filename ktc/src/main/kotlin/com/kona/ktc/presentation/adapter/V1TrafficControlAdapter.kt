package com.kona.ktc.presentation.adapter

import com.kona.ktc.application.usecase.TrafficControlUseCase
import com.kona.ktc.presentation.dto.mapper.TrafficMapper
import com.kona.ktc.presentation.dto.request.TrafficEntryRequest
import com.kona.ktc.presentation.dto.request.TrafficWaitRequest
import com.kona.ktc.presentation.dto.response.TrafficControlResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RequestMapping("/api/v1/traffic")
@RestController
class V1TrafficControlAdapter(
    private val trafficControlUseCase: TrafficControlUseCase,
    private val trafficMapper: TrafficMapper,
) {

    @PostMapping("/wait")
    suspend fun wait(@RequestBody request: TrafficWaitRequest): ResponseEntity<TrafficControlResponse> {
        val traffic = trafficMapper.toDomain(request)
        val waiting = trafficControlUseCase.controlTraffic(traffic)
        return trafficMapper.toResponse(traffic, waiting).success(HttpStatus.OK)
    }

    @PostMapping("/entry")
    suspend fun entry(@RequestBody request: TrafficEntryRequest): ResponseEntity<TrafficControlResponse> {
        val traffic = trafficMapper.toDomain(request)
        val waiting = trafficControlUseCase.controlTraffic(traffic)
        return trafficMapper.toResponse(traffic, waiting).success(HttpStatus.OK)
    }

}