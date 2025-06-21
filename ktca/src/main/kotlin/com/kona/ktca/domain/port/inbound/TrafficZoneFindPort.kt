package com.kona.ktca.domain.port.inbound

import com.kona.ktca.domain.dto.PageableDTO
import com.kona.ktca.domain.dto.TrafficZoneDTO
import com.kona.ktca.domain.model.TrafficZone
import org.springframework.data.domain.Page

interface TrafficZoneFindPort {

    suspend fun findTrafficZone(zoneId: String): TrafficZone

    suspend fun findPageTrafficZone(dto: TrafficZoneDTO, pageable: PageableDTO): Page<TrafficZone>

    suspend fun validateTrafficZoneId(zoneId: String)

}