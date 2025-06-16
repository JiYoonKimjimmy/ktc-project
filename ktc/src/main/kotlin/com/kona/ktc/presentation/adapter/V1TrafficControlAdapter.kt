package com.kona.ktc.presentation.adapter

import com.kona.ktc.application.usecase.TrafficControlStreamUseCase
import com.kona.ktc.application.usecase.TrafficControlUseCase
import com.kona.ktc.presentation.dto.mapper.TrafficMapper
import com.kona.ktc.presentation.dto.request.TrafficEntryRequest
import com.kona.ktc.presentation.dto.request.TrafficWaitRequest
import com.kona.ktc.presentation.dto.response.TrafficControlResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

@CrossOrigin(
    origins = ["*"],
    allowedHeaders = ["*"],
    methods = [RequestMethod.GET, RequestMethod.POST]
)
@RequestMapping("/api/v1/traffic")
@RestController
class V1TrafficControlAdapter(
    private val trafficControlUseCase: TrafficControlUseCase,
    private val trafficControlStreamUseCase: TrafficControlStreamUseCase,
    private val trafficMapper: TrafficMapper,
) {

    @PostMapping("/wait")
    suspend fun wait(@RequestBody request: TrafficWaitRequest): ResponseEntity<TrafficControlResponse> {
        val traffic = trafficMapper.toDomain(request.validate())
        val waiting = trafficControlUseCase.controlTraffic(traffic)
        return trafficMapper.toResponse(traffic, waiting).success(HttpStatus.OK)
    }

    @PostMapping("/entry")
    suspend fun entry(@RequestBody request: TrafficEntryRequest): ResponseEntity<TrafficControlResponse> {
        val traffic = trafficMapper.toDomain(request.validate())
        val waiting = trafficControlUseCase.controlTraffic(traffic)
        return trafficMapper.toResponse(traffic, waiting).success(HttpStatus.OK)
    }

    @GetMapping("/waiting")
    suspend fun waiting(
        @RequestParam(required = true) zoneId: String,
        @RequestParam(required = false) token: String?,
        @RequestParam(required = true) clientIP: String,
        @RequestParam(required = true) clientAgent: String,
    ): SseEmitter {
        val traffic = trafficMapper.toDomain(TrafficWaitRequest(zoneId, token, clientIP, clientAgent))
        return trafficControlStreamUseCase.controlTraffic(traffic, trafficMapper = trafficMapper)
    }

}