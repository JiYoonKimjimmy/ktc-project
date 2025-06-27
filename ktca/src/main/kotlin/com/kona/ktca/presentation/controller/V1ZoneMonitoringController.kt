package com.kona.ktca.presentation.controller

import com.kona.ktca.api.V1ZoneMonitoringApiDelegate
import com.kona.ktca.dto.V1ZoneMonitoringResponse
import com.kona.ktca.application.usecase.TrafficZoneMonitoringUseCase
import com.kona.ktca.presentation.model.V1ZoneMonitoringModelMapper
import kotlinx.coroutines.runBlocking
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

@RestController
class V1ZoneMonitoringController(
    private val trafficZoneMonitoringUseCase: TrafficZoneMonitoringUseCase,
    private val v1ZoneMonitoringModelMapper: V1ZoneMonitoringModelMapper
) : V1ZoneMonitoringApiDelegate {

    override fun zoneMonitoring(zoneId: String?, groupId: String?): ResponseEntity<V1ZoneMonitoringResponse> = runBlocking {
        trafficZoneMonitoringUseCase.trafficZoneMonitoring(zoneId, groupId)
            .map { v1ZoneMonitoringModelMapper.domainToModel(it) }
            .let { ResponseEntity.ok(V1ZoneMonitoringResponse(it)) }
    }

    override fun collectZoneMonitoring(zoneId: String?): ResponseEntity<V1ZoneMonitoringResponse> = runBlocking {
        trafficZoneMonitoringUseCase.collectTrafficZoneMonitoring(zoneId)
            .map { v1ZoneMonitoringModelMapper.domainToModel(it) }
            .let { ResponseEntity.ok(V1ZoneMonitoringResponse(it)) }
    }

}