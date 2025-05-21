package com.kona.ktca.v1.domain.port.outbound

import com.kona.ktca.v1.domain.dto.PageableDTO
import com.kona.ktca.v1.domain.dto.TrafficZoneDTO
import com.kona.ktca.v1.domain.model.TrafficZone
import org.springframework.data.domain.Page

interface TrafficZoneFindPort {

    suspend fun findTrafficZone(zoneId: String): TrafficZone

    suspend fun findAllTrafficZone(zoneId: String?): List<TrafficZone>

    suspend fun findPageTrafficZone(trafficZone: TrafficZoneDTO, pageable: PageableDTO): Page<TrafficZone>

}