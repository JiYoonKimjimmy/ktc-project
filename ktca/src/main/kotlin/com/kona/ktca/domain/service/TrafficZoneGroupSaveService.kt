package com.kona.ktca.domain.service

import com.kona.common.infrastructure.enumerate.TrafficZoneGroupStatus
import com.kona.common.infrastructure.error.ErrorCode
import com.kona.common.infrastructure.error.exception.InternalServiceException
import com.kona.common.infrastructure.error.exception.ResourceNotFoundException
import com.kona.ktca.domain.dto.TrafficZoneGroupDTO
import com.kona.ktca.domain.model.TrafficZoneGroup
import com.kona.ktca.domain.port.inbound.TrafficZoneGroupSavePort
import com.kona.ktca.domain.port.outbound.TrafficZoneGroupRepository
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service

@Service
class TrafficZoneGroupSaveService(
    private val trafficZoneGroupRepository: TrafficZoneGroupRepository
) : TrafficZoneGroupSavePort {

    override suspend fun create(name: String, order: Int?): TrafficZoneGroup {
        return order?.let { trafficZoneGroupRepository.save(TrafficZoneGroup.create(name, order)) }
            ?: trafficZoneGroupRepository.saveNextOrder(TrafficZoneGroup.create(name))
    }

    override suspend fun update(dto: TrafficZoneGroupDTO): TrafficZoneGroup {
        val group = findActiveTrafficZoneGroup(groupId = dto.groupId).update(dto)
        return trafficZoneGroupRepository.save(group)
    }

    override suspend fun delete(groupId: String) {
        try {
            findActiveTrafficZoneGroup(groupId)
                .let { trafficZoneGroupRepository.delete(it.groupId) }
        } catch (e: DataIntegrityViolationException) {
            // FK 제약조건 위반 무결성 에러 발생한 경우
            val detailMessage = e.cause?.message ?: "Cannot delete due to existing foreign key references."
            throw InternalServiceException(ErrorCode.TRAFFIC_ZONE_GROUP_CANNOT_BE_DELETED, detailMessage)
        }
    }

    private suspend fun findActiveTrafficZoneGroup(groupId: String?): TrafficZoneGroup {
        return groupId
            ?.let { TrafficZoneGroupDTO(groupId = it, status = TrafficZoneGroupStatus.ACTIVE) }
            ?.let { trafficZoneGroupRepository.findByPredicate(dto = it) }
            ?: throw ResourceNotFoundException(ErrorCode.TRAFFIC_ZONE_GROUP_NOT_FOUND)
    }

}