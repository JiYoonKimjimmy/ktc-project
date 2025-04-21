package com.kona.ktc.v0.presentation

import com.kona.ktc.v0.application.traffic.TrafficEntryUseCase
import com.kona.ktc.v0.application.traffic.TrafficWaitUseCase
import com.kona.ktc.v0.application.traffic.dto.TrafficEntryRequest
import com.kona.ktc.v0.application.traffic.dto.TrafficWaitRequest
import com.kona.ktc.v0.presentation.dto.TrafficEntryRequestDto
import com.kona.ktc.v0.presentation.dto.TrafficEntryResponseDto
import com.kona.ktc.v0.presentation.dto.TrafficWaitRequestDto
import com.kona.ktc.v0.presentation.dto.TrafficWaitResponseDto
import com.kona.ktc.v0.presentation.dto.TrafficWaitingDto
import com.kona.ktc.v0.presentation.dto.common.ResultDto
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v0/traffic")
class TrafficController(
    private val trafficWaitUseCase: TrafficWaitUseCase,
    private val trafficEntryUseCase: TrafficEntryUseCase
) {
    @PostMapping("/wait")
    suspend fun requestWait(@RequestBody request: TrafficWaitRequestDto): TrafficWaitResponseDto {
        val response = trafficWaitUseCase.execute(
            TrafficWaitRequest(
                zoneId = request.zoneId,
                token = request.token
            )
        )
        
        return TrafficWaitResponseDto(
            canEnter = response.canEnter,
            zoneId = response.zoneId,
            token = response.token,
            waiting = response.waiting?.let {
                TrafficWaitingDto(
                    number = it.number,
                    estimatedTime = it.estimatedTime,
                    totalCount = it.totalCount,
                    poolingPeriod = it.poolingPeriod
                )
            },
            result = ResultDto(status = "SUCCESS")
        )
    }

    @PostMapping("/entry")
    suspend fun requestEntry(@RequestBody request: TrafficEntryRequestDto): TrafficEntryResponseDto {
        val response = trafficEntryUseCase.execute(
            TrafficEntryRequest(
                zoneId = request.zoneId,
                token = request.token
            )
        )
        
        return TrafficEntryResponseDto(
            canEnter = response.canEnter,
            zoneId = response.zoneId,
            token = response.token,
            waiting = response.waiting?.let {
                TrafficWaitingDto(
                    number = it.number,
                    estimatedTime = it.estimatedTime,
                    totalCount = it.totalCount,
                    poolingPeriod = it.poolingPeriod
                )
            },
            result = ResultDto(status = "SUCCESS")
        )
    }
} 