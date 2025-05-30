package com.kona.ktca.application.usecase

import com.kona.ktca.domain.dto.PageableDTO
import com.kona.ktca.domain.dto.TrafficZoneDTO
import com.kona.ktca.domain.model.TrafficZone
import com.kona.ktca.domain.port.inbound.TrafficZoneCommandPort
import com.kona.ktca.domain.port.inbound.TrafficZoneReadPort
import com.kona.ktca.domain.port.outbound.TrafficZoneCachingPort
import org.springframework.data.domain.Page
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Transactional(readOnly = true)
@Component
class TrafficZoneManagementUseCase(
    private val trafficZoneCommandPort: TrafficZoneCommandPort,
    private val trafficZoneReadPort: TrafficZoneReadPort,
    private val trafficZoneCachingPort: TrafficZoneCachingPort,
) {

    @Transactional
    suspend fun saveTrafficZone(dto: TrafficZoneDTO): TrafficZone {
        return if (dto.isCreate) {
            trafficZoneCommandPort.create(dto)
        } else {
            trafficZoneCommandPort.update(dto)
        }
    }

    suspend fun findTrafficZone(zoneId: String): TrafficZone {
        return trafficZoneReadPort.findTrafficZone(zoneId)
    }

    suspend fun findPageTrafficZone(trafficZone: TrafficZoneDTO, pageable: PageableDTO): Page<TrafficZone> {
        return trafficZoneReadPort.findPageTrafficZone(trafficZone, pageable)
    }

    @Transactional
    suspend fun deleteTrafficZone(zoneId: String) {
        trafficZoneCommandPort.delete(zoneId)
    }

    suspend fun clearTrafficZone(zoneIds: List<String>) {
        trafficZoneCachingPort.clear(zoneIds)
    }

}