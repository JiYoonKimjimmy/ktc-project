package com.kona.ktca.domain.port.outbound

import com.kona.ktca.domain.dto.PageableDTO
import com.kona.ktca.domain.dto.TrafficZoneDTO
import com.kona.ktca.domain.model.TrafficZone
import org.springframework.data.domain.Page

interface TrafficZoneFindPort {

    suspend fun findTrafficZone(zoneId: String): TrafficZone?

    suspend fun findActiveTrafficZone(zoneId: String): TrafficZone?

    suspend fun findAllTrafficZone(zoneId: String?): List<TrafficZone>

    suspend fun findPageTrafficZone(trafficZone: TrafficZoneDTO, pageable: PageableDTO): Page<TrafficZone>

}