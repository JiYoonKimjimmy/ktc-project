package com.kona.ktca.domain.service

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
        return dto.toDomain()
            .let { trafficZoneSavePort.save(it) }
    }

    override suspend fun update(dto: TrafficZoneDTO): TrafficZone {
        return trafficZoneFindPort.findTrafficZone(dto.zoneId!!).update(dto)
            .let { trafficZoneSavePort.save(it) }
    }

    override suspend fun delete(zoneId: String) {
        trafficZoneFindPort.findTrafficZone(zoneId).delete()
            .let { trafficZoneSavePort.save(it) }
    }
}