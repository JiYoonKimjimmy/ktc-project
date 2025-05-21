package com.kona.ktca.v1.presentation.controller

import com.kona.common.infrastructure.enumerate.TrafficZoneStatus
import com.kona.common.infrastructure.util.convertPatternOf
import com.kona.ktca.api.V1ZoneManagementApiDelegate
import com.kona.ktca.dto.V1FindAllZoneResponse
import com.kona.ktca.dto.V1FindZoneResponse
import com.kona.ktca.dto.V1SaveZoneRequest
import com.kona.ktca.dto.V1SaveZoneResponse
import com.kona.ktca.v1.application.dto.TrafficZoneDTO
import com.kona.ktca.v1.application.usecase.TrafficZoneManagementUseCase
import kotlinx.coroutines.runBlocking
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import java.time.LocalDateTime

@Controller
class V1ZoneManagementController(
    private val trafficZoneManagementUseCase: TrafficZoneManagementUseCase
) : V1ZoneManagementApiDelegate {

    override fun saveZone(v1SaveZoneRequest: V1SaveZoneRequest): ResponseEntity<V1SaveZoneResponse> = runBlocking {
        val dto = TrafficZoneDTO(
            zoneId = v1SaveZoneRequest.zoneId,
            zoneAlias = v1SaveZoneRequest.zoneAlias,
            threshold = v1SaveZoneRequest.threshold?.toLong(),
            activationTime = v1SaveZoneRequest.activationTime?.convertPatternOf() ?: LocalDateTime.now(),
            status = v1SaveZoneRequest.status?.let { TrafficZoneStatus.valueOf(it.name) } ?: TrafficZoneStatus.ACTIVE
        )
        val result = trafficZoneManagementUseCase.saveTrafficZone(dto)
        val httpStatus = if (dto.isCreate) HttpStatus.CREATED else HttpStatus.OK
        ResponseEntity(V1SaveZoneResponse(zoneId = result.zoneId),  httpStatus)
    }

    override fun findZone(zoneId: String): ResponseEntity<V1FindZoneResponse> {
        return super.findZone(zoneId)
    }

    override fun findZoneList(page: Int?, size: Int?, zoneId: String?): ResponseEntity<V1FindAllZoneResponse> {
        return super.findZoneList(page, size, zoneId)
    }

    override fun deleteZone(zoneId: String): ResponseEntity<Unit> {
        return super.deleteZone(zoneId)
    }

}