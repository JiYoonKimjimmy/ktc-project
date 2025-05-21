package com.kona.ktca.v1.domain.service

import com.kona.ktca.v1.domain.model.TrafficZone
import com.kona.ktca.v1.domain.port.inbound.TrafficZoneReadPort
import com.kona.ktca.v1.domain.port.outbound.TrafficZoneFindPort
import com.kona.ktca.v1.domain.port.outbound.TrafficZoneWaitingFindPort
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

}