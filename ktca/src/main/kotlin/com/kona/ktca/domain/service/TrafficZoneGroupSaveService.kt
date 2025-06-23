package com.kona.ktca.domain.service

import com.kona.common.infrastructure.enumerate.TrafficZoneGroupStatus
import com.kona.common.infrastructure.error.ErrorCode
import com.kona.common.infrastructure.error.exception.ResourceNotFoundException
import com.kona.ktca.domain.dto.TrafficZoneGroupDTO
import com.kona.ktca.domain.model.TrafficZoneGroup
import com.kona.ktca.domain.port.inbound.TrafficZoneGroupSavePort
import com.kona.ktca.domain.port.outbound.TrafficZoneGroupRepository
import org.springframework.stereotype.Service

@Service
class TrafficZoneGroupSaveService(
    private val trafficZoneGroupRepository: TrafficZoneGroupRepository
) : TrafficZoneGroupSavePort {

    override suspend fun create(name: String): TrafficZoneGroup {
        return trafficZoneGroupRepository.saveNextOrder(name)
    }

    override suspend fun update(dto: TrafficZoneGroupDTO): TrafficZoneGroup {
        val group = findActiveTrafficZoneGroup(groupId = dto.groupId).update(dto)
        return trafficZoneGroupRepository.save(group)
    }

    private suspend fun findActiveTrafficZoneGroup(groupId: Long?): TrafficZoneGroup {
        return groupId
            ?.let { trafficZoneGroupRepository.findByGroupIdAndStatus(groupId = it, status = TrafficZoneGroupStatus.ACTIVE) }
            ?: throw ResourceNotFoundException(ErrorCode.TRAFFIC_ZONE_GROUP_NOT_FOUND)
    }

}