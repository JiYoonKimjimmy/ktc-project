package com.kona.ktca.application.usecase

import com.kona.ktca.domain.dto.PageableDTO
import com.kona.ktca.domain.dto.TrafficZoneDTO
import com.kona.ktca.domain.model.TrafficZone
import com.kona.ktca.domain.port.inbound.TrafficZoneSavePort
import com.kona.ktca.domain.port.inbound.TrafficZoneFindPort
import com.kona.ktca.domain.port.outbound.TrafficZoneCachingPort
import org.springframework.data.domain.Page
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Transactional(readOnly = true)
@Component
class TrafficZoneManagementUseCase(
    private val trafficZoneSavePort: TrafficZoneSavePort,
    private val trafficZoneFindPort: TrafficZoneFindPort,
    private val trafficZoneCachingPort: TrafficZoneCachingPort,
) {

    @Transactional
    suspend fun createTrafficZone(dto: TrafficZoneDTO): TrafficZone {
        // 요청 `zoneId` 포함되는 경우, `zoneId` 중복 검증 처리
        dto.zoneId?.let { trafficZoneFindPort.validateTrafficZoneId(it) }
        return trafficZoneSavePort.create(dto)
    }

    suspend fun findTrafficZone(zoneId: String): TrafficZone {
        return trafficZoneFindPort.findTrafficZone(zoneId)
    }

    suspend fun findPageTrafficZone(trafficZone: TrafficZoneDTO, pageable: PageableDTO): Page<TrafficZone> {
        return trafficZoneFindPort.findPageTrafficZone(trafficZone, pageable)
    }

    @Transactional
    suspend fun updateTrafficZone(zoneId: String, dto: TrafficZoneDTO): TrafficZone {
        val zone = trafficZoneFindPort.findTrafficZone(zoneId)
        return trafficZoneSavePort.update(zone, dto)
    }

    @Transactional
    suspend fun deleteTrafficZone(zoneId: String) {
        trafficZoneSavePort.delete(zoneId)
    }

    suspend fun clearTrafficZone(zoneIds: List<String>) {
        trafficZoneCachingPort.clearAll(zoneIds)
    }

}