package com.kona.ktca.v1.application.controller

import com.kona.ktca.api.V1ZoneMonitoringApiDelegate
import com.kona.ktca.dto.V1ZoneMonitoringResponse
import com.kona.ktca.v1.application.model.V1ZoneMonitoringModelMapper
import com.kona.ktca.v1.domain.port.inbound.TrafficZoneMonitorPort
import kotlinx.coroutines.runBlocking
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

@RestController
class V1ZoneMonitoringController(
    private val v1ZoneMonitoringModelMapper: V1ZoneMonitoringModelMapper,
    private val trafficZoneMonitorPort: TrafficZoneMonitorPort
) : V1ZoneMonitoringApiDelegate {

    override fun zoneMonitoring(zoneId: String?): ResponseEntity<V1ZoneMonitoringResponse> = runBlocking {
        trafficZoneMonitorPort.monitoring(zoneId)
            .let { v1ZoneMonitoringModelMapper.domainToContent(it) }
            .let { ResponseEntity.ok(V1ZoneMonitoringResponse(it)) }
    }

}