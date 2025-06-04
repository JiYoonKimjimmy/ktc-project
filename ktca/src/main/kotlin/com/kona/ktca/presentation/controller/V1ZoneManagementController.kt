package com.kona.ktca.presentation.controller

import com.kona.common.infrastructure.enumerate.TrafficZoneStatus
import com.kona.common.infrastructure.util.convertPatternOf
import com.kona.ktca.api.V1ZoneManagementApiDelegate
import com.kona.ktca.application.usecase.TrafficZoneManagementUseCase
import com.kona.ktca.domain.dto.PageableDTO
import com.kona.ktca.domain.dto.TrafficZoneDTO
import com.kona.ktca.dto.*
import com.kona.ktca.presentation.model.V1ZoneModelMapper
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

    override fun createZone(v1CreateZoneRequest: V1CreateZoneRequest): ResponseEntity<V1CreateZoneResponse> = runBlocking {
        val dto = TrafficZoneDTO(
            zoneId = v1CreateZoneRequest.zoneId,
            zoneAlias = v1CreateZoneRequest.zoneAlias,
            threshold = v1CreateZoneRequest.threshold.toLong(),
            activationTime = v1CreateZoneRequest.activationTime?.convertPatternOf() ?: LocalDateTime.now(),
            status = v1CreateZoneRequest.status?.let { TrafficZoneStatus.valueOf(it.name) } ?: TrafficZoneStatus.ACTIVE
        )
        val result = trafficZoneManagementUseCase.createTrafficZone(dto)
        ResponseEntity(V1CreateZoneResponse(zoneId = result.zoneId),  HttpStatus.CREATED)
    }

    override fun findZone(zoneId: String): ResponseEntity<V1FindZoneResponse> = runBlocking {
        val result = trafficZoneManagementUseCase.findTrafficZone(zoneId)
        ResponseEntity(V1FindZoneResponse(data = v1ZoneModelMapper.domainToModel(result)), HttpStatus.OK)
    }

    override fun findZoneList(page: Int?, size: Int?, zoneId: String?, status: String?): ResponseEntity<V1FindAllZoneResponse> = runBlocking {
        val trafficZone = TrafficZoneDTO(zoneId = zoneId, status = status?.let(TrafficZoneStatus::valueOf))
        val pageable = PageableDTO(number = page ?: 0, size = size ?: 20)
        val result = trafficZoneManagementUseCase.findPageTrafficZone(trafficZone, pageable)
        val response = V1FindAllZoneResponse(
            pageable = Pageable(
                first = result.isFirst,
                last = result.isLast,
                number = result.number,
                numberOfElements = result.numberOfElements,
                propertySize = result.size,
                totalPages = result.totalPages,
                totalElements = result.totalElements,
            ),
            content = result.content.map { v1ZoneModelMapper.domainToModel(it) }
        )
        ResponseEntity(response, HttpStatus.OK)
    }

    override fun updateZone(zoneId: String, v1UpdateZoneRequest: V1UpdateZoneRequest): ResponseEntity<V1UpdateZoneResponse> = runBlocking {
        val dto = TrafficZoneDTO(
            zoneAlias = v1UpdateZoneRequest.zoneAlias,
            threshold = v1UpdateZoneRequest.threshold?.toLong(),
            activationTime = v1UpdateZoneRequest.activationTime?.convertPatternOf() ?: LocalDateTime.now(),
            status = v1UpdateZoneRequest.status?.let { TrafficZoneStatus.valueOf(it.name) } ?: TrafficZoneStatus.ACTIVE
        )
        val result = trafficZoneManagementUseCase.updateTrafficZone(zoneId, dto)
        ResponseEntity(V1UpdateZoneResponse(zoneId = result.zoneId),  HttpStatus.OK)
    }

    override fun deleteZone(zoneId: String): ResponseEntity<V1DeleteZoneResponse> = runBlocking {
        trafficZoneManagementUseCase.deleteTrafficZone(zoneId)
        ResponseEntity(V1DeleteZoneResponse(), HttpStatus.OK)
    }

    override fun clearZoneCache(v1ClearZoneCacheRequest: V1ClearZoneCacheRequest?): ResponseEntity<V1ClearZoneCacheResponse> = runBlocking {
        trafficZoneManagementUseCase.clearTrafficZone(v1ClearZoneCacheRequest?.zoneIds ?: emptyList())
        ResponseEntity(V1ClearZoneCacheResponse(), HttpStatus.OK)
    }

}