package com.kona.ktca.domain.service

import com.kona.common.infrastructure.enumerate.TrafficZoneStatus
import com.kona.common.infrastructure.error.ErrorCode
import com.kona.common.infrastructure.error.exception.InternalServiceException
import com.kona.common.infrastructure.error.exception.ResourceNotFoundException
import com.kona.ktca.domain.dto.TrafficZoneDTO
import com.kona.ktca.domain.model.TrafficZone
import com.kona.ktca.domain.port.inbound.TrafficZoneCommandPort
import com.kona.ktca.domain.port.outbound.TrafficZoneCachingPort
import com.kona.ktca.domain.port.outbound.TrafficZoneFindPort
import com.kona.ktca.domain.port.outbound.TrafficZoneSavePort
import org.springframework.stereotype.Service

@Service
class TrafficZoneCommandService(
    private val trafficZoneSavePort: TrafficZoneSavePort,
    private val trafficZoneFindPort: TrafficZoneFindPort,
    private val trafficZoneCachingPort: TrafficZoneCachingPort
) : TrafficZoneCommandPort {

    override suspend fun create(dto: TrafficZoneDTO): TrafficZone {
        return TrafficZone.create(dto).let { trafficZoneSavePort.save(it) }
    }

    override suspend fun update(zone: TrafficZone, dto: TrafficZoneDTO): TrafficZone {
        if (zone.status == TrafficZoneStatus.DELETED) {
            // 이미 zone 상태 'DELETED' 인 경우, 수정 불가 처리
            throw InternalServiceException(ErrorCode.DELETED_TRAFFIC_ZONE_CANNOT_BE_CHANGED)
        }
        return zone.update(dto).let { trafficZoneSavePort.save(it) }
    }

    override suspend fun delete(zoneId: String) {
        trafficZoneFindPort.findTrafficZone(zoneId)
            ?.delete()
            ?.let { trafficZoneSavePort.save(it) }
            ?.let { trafficZoneCachingPort.clearAll(listOf(it.zoneId)) }
            ?: throw ResourceNotFoundException(ErrorCode.TRAFFIC_ZONE_NOT_FOUND)
    }

    override suspend fun validateTrafficZoneId(zoneId: String) {
        val zone = trafficZoneFindPort.findActiveTrafficZone(zoneId)
        if (zone != null) {
            // 이미 동일한 `zoneId` 기준 Zone 정보 있는 경우, 예외 처리
            throw InternalServiceException(ErrorCode.TRAFFIC_ZONE_ALREADY_EXISTS)
        }
    }
}