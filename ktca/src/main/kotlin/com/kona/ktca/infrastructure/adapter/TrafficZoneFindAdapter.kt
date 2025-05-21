package com.kona.ktca.infrastructure.adapter

import com.kona.common.infrastructure.enumerate.TrafficZoneStatus.ACTIVE
import com.kona.common.infrastructure.error.ErrorCode
import com.kona.common.infrastructure.error.exception.ResourceNotFoundException
import com.kona.ktca.domain.dto.PageableDTO
import com.kona.ktca.domain.dto.TrafficZoneDTO
import com.kona.ktca.domain.model.TrafficZone
import com.kona.ktca.domain.port.outbound.TrafficZoneFindPort
import com.kona.ktca.infrastructure.repository.TrafficZoneRepository
import org.springframework.data.domain.Page
import org.springframework.stereotype.Component

@Component
class TrafficZoneFindAdapter(
    private val trafficZoneRepository: TrafficZoneRepository
) : TrafficZoneFindPort {

    override suspend fun findTrafficZone(zoneId: String): TrafficZone {
        return trafficZoneRepository.findByZoneId(zoneId)
            .takeIf { it != null }
            ?.toDomain()
            ?: throw ResourceNotFoundException(ErrorCode.TRAFFIC_ZONE_NOT_FOUND)
    }

    override suspend fun findAllTrafficZone(zoneId: String?): List<TrafficZone> {
        return trafficZoneRepository.findAllByStatus(ACTIVE)
            .filter { if (zoneId != null) it.id == zoneId else true }
            .map { it.toDomain() }
    }

    override suspend fun findPageTrafficZone(trafficZone: TrafficZoneDTO, pageable: PageableDTO): Page<TrafficZone> {
        return trafficZoneRepository.findPage(trafficZone.toPredicatable(), pageable.toPageRequest())
            .map { it?.toDomain() }
    }

}