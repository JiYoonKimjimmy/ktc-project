package com.kona.ktca.domain.service

import com.kona.ktca.domain.dto.PageableDTO
import com.kona.ktca.domain.dto.TrafficZoneDTO
import com.kona.ktca.domain.model.TrafficZone
import com.kona.ktca.domain.port.inbound.TrafficZoneReadPort
import com.kona.ktca.domain.port.outbound.TrafficZoneFindPort
import com.kona.ktca.domain.port.outbound.TrafficZoneWaitingFindPort
import org.springframework.data.domain.Page
import org.springframework.stereotype.Service

@Service
class TrafficZoneReadService(
    private val trafficZoneFindPort: TrafficZoneFindPort,
    private val trafficZoneWaitingFindPort: TrafficZoneWaitingFindPort
) : TrafficZoneReadPort {

    override suspend fun findTrafficZone(zoneId: String): TrafficZone {
        return trafficZoneFindPort.findTrafficZone(zoneId)
    }

    override suspend fun findTrafficZones(zoneId: String?, includeWaiting: Boolean): List<TrafficZone> {
        val zones = trafficZoneFindPort.findAllTrafficZone(zoneId)
        return if (includeWaiting) {
            zones.map { it.applyWaiting(trafficZoneWaitingFindPort::findTrafficZoneWaiting) }
        } else {
            zones
        }
    }

    override suspend fun findPageTrafficZone(trafficZone: TrafficZoneDTO, pageable: PageableDTO): Page<TrafficZone> {
        return trafficZoneFindPort.findPageTrafficZone(trafficZone, pageable)
    }

}