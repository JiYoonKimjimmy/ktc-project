package com.kona.ktca.presentation.controller

import com.kona.ktca.api.V1ZoneGroupManagementApiDelegate
import com.kona.ktca.application.usecase.TrafficZoneGroupManagementUseCase
import com.kona.ktca.domain.dto.TrafficZoneGroupDTO
import com.kona.ktca.dto.*
import com.kona.ktca.presentation.model.V1ZoneGroupModelMapper
import kotlinx.coroutines.runBlocking
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

@RestController
class V1ZoneGroupManagementController(
    private val trafficZoneGroupManagementUseCase: TrafficZoneGroupManagementUseCase,
    private val v1ZoneGroupModelMapper: V1ZoneGroupModelMapper
)  : V1ZoneGroupManagementApiDelegate {

    override fun createZoneGroup(
        v1CreateZoneGroupRequest: V1CreateZoneGroupRequest,
        xKTCMemberId: Long?,
    ): ResponseEntity<V1CreateZoneGroupResponse> = runBlocking {
        val result = trafficZoneGroupManagementUseCase.createTrafficZoneGroup(v1CreateZoneGroupRequest.groupName)
        val response = V1CreateZoneGroupResponse(groupId = result)
        ResponseEntity(response, HttpStatus.CREATED)
    }

    override fun findZoneGroupList(): ResponseEntity<V1FindZoneGroupListResponse> = runBlocking {
        val result = trafficZoneGroupManagementUseCase.findAllTrafficZoneGroup().map { v1ZoneGroupModelMapper.domainToModel(it) }
        val response = V1FindZoneGroupListResponse(content = result)
        ResponseEntity(response, HttpStatus.OK)
    }

    override fun updateZoneGroup(
        groupId: Long,
        v1UpdateZoneGroupRequest: V1UpdateZoneGroupRequest,
        xKTCMemberId: Long?,
    ): ResponseEntity<V1UpdateZoneGroupResponse> = runBlocking {
        val dto = TrafficZoneGroupDTO(
            groupId = groupId,
            name = v1UpdateZoneGroupRequest.groupName,
            order = v1UpdateZoneGroupRequest.groupOrder
        )
        val result = trafficZoneGroupManagementUseCase.updateTrafficZoneGroup(dto)
        val response = V1UpdateZoneGroupResponse(groupId = result)
        ResponseEntity(response, HttpStatus.OK)
    }

    override fun deleteZoneGroup(
        groupId: Long,
        xKTCMemberId: Long?,
    ): ResponseEntity<V1DeleteZoneGroupResponse> = runBlocking {
        trafficZoneGroupManagementUseCase.deleteTrafficZoneGroup(groupId)
        ResponseEntity(V1DeleteZoneGroupResponse(), HttpStatus.OK)
    }

}