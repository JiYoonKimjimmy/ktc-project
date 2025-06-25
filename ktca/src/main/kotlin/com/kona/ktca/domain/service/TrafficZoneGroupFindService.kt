package com.kona.ktca.domain.service

import com.kona.common.infrastructure.enumerate.TrafficZoneGroupStatus
import com.kona.common.infrastructure.error.ErrorCode
import com.kona.common.infrastructure.error.exception.ResourceNotFoundException
import com.kona.ktca.domain.dto.TrafficZoneGroupDTO
import com.kona.ktca.domain.model.TrafficZoneGroup
import com.kona.ktca.domain.port.inbound.TrafficZoneGroupFindPort
import com.kona.ktca.domain.port.outbound.TrafficZoneGroupRepository
import org.springframework.stereotype.Service

@Service
class TrafficZoneGroupFindService(
    private val trafficZoneGroupRepository: TrafficZoneGroupRepository
) : TrafficZoneGroupFindPort {

    override suspend fun findTrafficZoneGroup(dto: TrafficZoneGroupDTO): TrafficZoneGroup {
        return trafficZoneGroupRepository.findByPredicate(dto)
            ?: throw ResourceNotFoundException(ErrorCode.TRAFFIC_ZONE_GROUP_NOT_FOUND)
    }

    override suspend fun findAllTrafficZoneGroup(): List<TrafficZoneGroup> {
        return trafficZoneGroupRepository.findAllByStatus(status = TrafficZoneGroupStatus.ACTIVE)
    }

}