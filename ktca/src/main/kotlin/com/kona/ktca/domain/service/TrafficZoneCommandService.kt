package com.kona.ktca.domain.service

import com.kona.common.infrastructure.error.ErrorCode
import com.kona.common.infrastructure.error.exception.InternalServiceException
import com.kona.common.infrastructure.error.exception.ResourceNotFoundException
import com.kona.ktca.domain.dto.TrafficZoneDTO
import com.kona.ktca.domain.model.TrafficZone
import com.kona.ktca.domain.port.inbound.TrafficZoneCommandPort
import com.kona.ktca.domain.port.outbound.TrafficZoneFindPort
import com.kona.ktca.domain.port.outbound.TrafficZoneSavePort
import org.springframework.stereotype.Service

@Service
class TrafficZoneCommandService(
    private val trafficZoneSavePort: TrafficZoneSavePort,
    private val trafficZoneFindPort: TrafficZoneFindPort
) : TrafficZoneCommandPort {

    override suspend fun create(dto: TrafficZoneDTO): TrafficZone {
        return TrafficZone.create(dto).let { trafficZoneSavePort.save(it) }
    }

    override suspend fun update(zone: TrafficZone, dto: TrafficZoneDTO): TrafficZone {
        return zone.update(dto).let { trafficZoneSavePort.save(it) }
    }

    override suspend fun delete(zoneId: String) {
        trafficZoneFindPort.findTrafficZone(zoneId)
            ?.delete()
            ?.let { trafficZoneSavePort.save(it) }
            ?: throw ResourceNotFoundException(ErrorCode.TRAFFIC_ZONE_NOT_FOUND)
    }

    override suspend fun validateTrafficZoneId(zoneId: String) {
        val zone = trafficZoneFindPort.findTrafficZone(zoneId)
        if (zone != null) {
            // 이미 동일한 `zoneId` 기준 Zone 정보 있는 경우, 예외 처리
            throw InternalServiceException(ErrorCode.TRAFFIC_ZONE_ALREADY_EXISTS)
        }
    }
}