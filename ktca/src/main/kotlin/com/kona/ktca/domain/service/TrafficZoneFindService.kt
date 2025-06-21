package com.kona.ktca.domain.service

import com.kona.common.infrastructure.enumerate.TrafficZoneStatus.DELETED
import com.kona.common.infrastructure.error.ErrorCode
import com.kona.common.infrastructure.error.exception.InternalServiceException
import com.kona.common.infrastructure.error.exception.ResourceNotFoundException
import com.kona.ktca.domain.dto.PageableDTO
import com.kona.ktca.domain.dto.TrafficZoneDTO
import com.kona.ktca.domain.model.TrafficZone
import com.kona.ktca.domain.port.inbound.TrafficZoneFindPort
import com.kona.ktca.domain.port.outbound.TrafficZoneRepository
import org.springframework.data.domain.Page
import org.springframework.stereotype.Service

@Service
class TrafficZoneFindService(
    private val trafficZoneRepository: TrafficZoneRepository
) : TrafficZoneFindPort {

    override suspend fun findTrafficZone(zoneId: String): TrafficZone {
        return trafficZoneRepository.findByZoneId(zoneId)  ?: throw ResourceNotFoundException(ErrorCode.TRAFFIC_ZONE_NOT_FOUND)
    }

    override suspend fun findPageTrafficZone(dto: TrafficZoneDTO, pageable: PageableDTO): Page<TrafficZone> {
        return trafficZoneRepository.findPage(dto, pageable)
    }

    override suspend fun validateTrafficZoneId(zoneId: String) {
        val zone = trafficZoneRepository.findByZoneIdAndStatusNot(zoneId = zoneId, status = DELETED)
        if (zone != null) {
            // 이미 동일한 `zoneId` 기준 Zone 정보 있는 경우, 예외 처리
            throw InternalServiceException(ErrorCode.TRAFFIC_ZONE_ALREADY_EXISTS)
        }
    }

}