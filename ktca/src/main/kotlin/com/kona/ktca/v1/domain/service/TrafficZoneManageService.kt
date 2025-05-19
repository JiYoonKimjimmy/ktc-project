package com.kona.ktca.v1.domain.service

import com.kona.ktca.v1.domain.model.TrafficZone
import com.kona.ktca.v1.domain.port.dto.TrafficZoneDTO
import com.kona.ktca.v1.domain.port.inbound.TrafficZoneManagePort
import com.kona.ktca.v1.domain.port.outbound.TrafficZoneFindPort
import com.kona.ktca.v1.domain.port.outbound.TrafficZoneSavePort
import org.springframework.stereotype.Service

@Service
class TrafficZoneManageService(
    private val trafficZoneSavePort: TrafficZoneSavePort,
    private val trafficZoneFindPort: TrafficZoneFindPort
) : TrafficZoneManagePort {

    override suspend fun save(dto: TrafficZoneDTO): TrafficZone {
        val trafficZone = if (dto.isUpdate) {
            // traffic zone 변경인 경우, 조회 후 정보 변경 처리
            trafficZoneFindPort.findTrafficZone(dto.zoneId!!).update(dto)
        } else {
            dto.toDomain()
        }
        return trafficZoneSavePort.save(trafficZone)
    }

}