package com.kona.ktca.v1.presentation.controller

import com.kona.common.infrastructure.enumerate.TrafficZoneStatus
import com.kona.common.infrastructure.util.convertPatternOf
import com.kona.ktca.api.V1ZoneManagementApiDelegate
import com.kona.ktca.dto.*
import com.kona.ktca.v1.domain.dto.TrafficZoneDTO
import com.kona.ktca.v1.application.usecase.TrafficZoneManagementUseCase
import com.kona.ktca.v1.presentation.model.V1ZoneModelMapper
import kotlinx.coroutines.runBlocking
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import java.time.LocalDateTime

@Controller
class V1ZoneManagementController(
    private val trafficZoneManagementUseCase: TrafficZoneManagementUseCase,
    private val v1ZoneModelMapper: V1ZoneModelMapper
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

    override fun findZone(zoneId: String): ResponseEntity<V1FindZoneResponse> = runBlocking {
        val result = trafficZoneManagementUseCase.findTrafficZone(zoneId)
        ResponseEntity(V1FindZoneResponse(data = v1ZoneModelMapper.domainToModel(result)), HttpStatus.OK)
    }

    override fun findZoneList(page: Int?, size: Int?, zoneId: String?, status: String?): ResponseEntity<V1FindAllZoneResponse> {
        return super.findZoneList(page, size, zoneId, status)
    }

    override fun deleteZone(zoneId: String): ResponseEntity<V1DeleteZoneResponse> = runBlocking {
        trafficZoneManagementUseCase.deleteTrafficZone(zoneId)
        ResponseEntity(V1DeleteZoneResponse(), HttpStatus.OK)
    }

}