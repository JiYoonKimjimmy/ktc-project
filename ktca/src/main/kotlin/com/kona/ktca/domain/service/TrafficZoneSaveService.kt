package com.kona.ktca.domain.service

import com.kona.common.infrastructure.enumerate.TrafficZoneGroupStatus
import com.kona.common.infrastructure.enumerate.TrafficZoneStatus
import com.kona.common.infrastructure.error.ErrorCode
import com.kona.common.infrastructure.error.exception.InternalServiceException
import com.kona.common.infrastructure.error.exception.ResourceNotFoundException
import com.kona.common.infrastructure.util.DEFAULT_ZONE_GROUP_ID
import com.kona.ktca.domain.dto.TrafficZoneDTO
import com.kona.ktca.domain.model.TrafficZone
import com.kona.ktca.domain.model.TrafficZoneGroup
import com.kona.ktca.domain.port.inbound.TrafficZoneSavePort
import com.kona.ktca.domain.port.outbound.TrafficZoneCachingPort
import com.kona.ktca.domain.port.outbound.TrafficZoneGroupRepository
import com.kona.ktca.domain.port.outbound.TrafficZoneRepository
import org.springframework.stereotype.Service

@Service
class TrafficZoneSaveService(
    private val trafficZoneRepository: TrafficZoneRepository,
    private val trafficZoneCachingPort: TrafficZoneCachingPort,
    private val trafficZoneGroupRepository: TrafficZoneGroupRepository
) : TrafficZoneSavePort {

    override suspend fun create(dto: TrafficZoneDTO): TrafficZone {
        val group = findTrafficZoneGroup(groupId = dto.groupId ?: DEFAULT_ZONE_GROUP_ID)
        return TrafficZone.create(dto = dto.applyGroup(group)).saveTrafficZone()
    }

    override suspend fun update(zone: TrafficZone, dto: TrafficZoneDTO): TrafficZone {
        if (zone.status == TrafficZoneStatus.DELETED) {
            // 이미 zone 상태 'DELETED' 인 경우, 수정 불가 처리
            throw InternalServiceException(ErrorCode.DELETED_TRAFFIC_ZONE_CANNOT_BE_CHANGED)
        }
        val group = findTrafficZoneGroup(groupId = dto.groupId ?: zone.groupId)
        return zone.update(dto = dto.applyGroup(group)).saveTrafficZone()
    }

    override suspend fun delete(zoneId: String): TrafficZone {
        return trafficZoneRepository.findByZoneId(zoneId)
            ?.delete()
            ?.saveTrafficZone()
            ?.also { trafficZoneCachingPort.clearAll(listOf(it.zoneId)) }
            ?: throw ResourceNotFoundException(ErrorCode.TRAFFIC_ZONE_NOT_FOUND)
    }

    private suspend fun findTrafficZoneGroup(groupId: Long): TrafficZoneGroup {
        return trafficZoneGroupRepository.findByGroupIdAndStatus(groupId = groupId, status = TrafficZoneGroupStatus.ACTIVE)
            ?: throw ResourceNotFoundException(ErrorCode.TRAFFIC_ZONE_GROUP_NOT_FOUND)
    }

    private suspend fun TrafficZone.saveTrafficZone(): TrafficZone {
        return trafficZoneRepository.save(zone = this)
            .also { trafficZoneCachingPort.save(zone = it) }
    }

}