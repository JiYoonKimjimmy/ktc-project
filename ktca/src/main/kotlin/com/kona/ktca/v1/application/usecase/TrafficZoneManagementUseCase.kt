package com.kona.ktca.v1.application.usecase

import com.kona.ktca.v1.application.dto.TrafficZoneDTO
import com.kona.ktca.v1.domain.model.TrafficZone
import com.kona.ktca.v1.domain.port.inbound.TrafficZoneCommandPort
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class TrafficZoneManagementUseCase(
    private val trafficZoneCommandPort: TrafficZoneCommandPort
) {

    @Transactional
    suspend fun saveTrafficZone(dto: TrafficZoneDTO): TrafficZone {
        return if (dto.isCreate) {
            trafficZoneCommandPort.create(dto)
        } else {
            trafficZoneCommandPort.update(dto)
        }
    }

}