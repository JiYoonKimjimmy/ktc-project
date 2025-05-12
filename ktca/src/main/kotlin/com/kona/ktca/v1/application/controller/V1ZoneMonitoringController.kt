package com.kona.ktca.v1.application.controller

import com.kona.common.infrastructure.util.convertPatternOf
import com.kona.ktca.api.V1ZoneMonitoringApiDelegate
import com.kona.ktca.dto.V1ZoneMonitoringData
import com.kona.ktca.dto.V1ZoneMonitoringDataWaiting
import com.kona.ktca.dto.V1ZoneMonitoringResponse
import com.kona.ktca.dto.ZoneStatus
import com.kona.ktca.v1.domain.port.inbound.TrafficZoneMonitorPort
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

@RestController
class V1ZoneMonitoringController(
    private val trafficZoneMonitorPort: TrafficZoneMonitorPort
) : V1ZoneMonitoringApiDelegate {

    override fun zoneMonitoring(zoneId: String?): ResponseEntity<V1ZoneMonitoringResponse> = runBlocking {
        val content = trafficZoneMonitorPort.monitoring(zoneId)
            .zones
            .map {
                V1ZoneMonitoringData(
                    zoneId = it.zoneId,
                    zoneAlias = it.name,
                    threshold = it.threshold.toInt(),
                    activationTime = it.activationTime.convertPatternOf(),
                    status = ZoneStatus.valueOf(it.status.name),
                    waiting = V1ZoneMonitoringDataWaiting(
                        waitingCount = it.waiting.waitingCount.toInt(),
                        entryCount = it.waiting.entryCount.toInt(),
                        estimatedClearTime = it.waiting.estimatedClearTime.toInt()
                    ),
                )
            }
        ResponseEntity.ok(V1ZoneMonitoringResponse(content))
    }

}