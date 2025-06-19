package com.kona.ktca.infrastructure.adapter

import com.kona.common.infrastructure.enumerate.TrafficZoneStatus.ACTIVE
import com.kona.common.infrastructure.enumerate.TrafficZoneStatus.DELETED
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

    override suspend fun findTrafficZone(zoneId: String): TrafficZone? {
        return trafficZoneRepository.findByZoneId(zoneId)
            .takeIf { it != null }
            ?.toDomain()
    }

    override suspend fun findActiveTrafficZone(zoneId: String): TrafficZone? {
        return trafficZoneRepository.findByZoneIdAndStatusNot(zoneId, DELETED)?.toDomain()
    }

    override suspend fun findAllActiveTrafficZone(zoneId: String?): List<TrafficZone> {
        return trafficZoneRepository.findAllByStatus(ACTIVE)
            .filter { if (zoneId != null) it.id == zoneId else true }
            .map { it.toDomain() }
    }

    override suspend fun findPageTrafficZone(dto: TrafficZoneDTO, pageable: PageableDTO): Page<TrafficZone> {
        return trafficZoneRepository.findPage(dto.toPredicatable(), pageable.toPageRequest())
            .map { it?.toDomain() }
    }

}