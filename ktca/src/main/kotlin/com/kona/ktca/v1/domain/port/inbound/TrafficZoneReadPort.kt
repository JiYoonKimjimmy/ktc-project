package com.kona.ktca.v1.domain.port.inbound

import com.kona.ktca.v1.domain.dto.PageableDTO
import com.kona.ktca.v1.domain.dto.TrafficZoneDTO
import com.kona.ktca.v1.domain.model.TrafficZone
import org.springframework.data.domain.Page

interface TrafficZoneReadPort {

    suspend fun findTrafficZone(zoneId: String): TrafficZone

    suspend fun findTrafficZones(zoneId: String?, includeWaiting: Boolean = false): List<TrafficZone>

    suspend fun findPageTrafficZone(trafficZone: TrafficZoneDTO, pageable: PageableDTO): Page<TrafficZone>

}