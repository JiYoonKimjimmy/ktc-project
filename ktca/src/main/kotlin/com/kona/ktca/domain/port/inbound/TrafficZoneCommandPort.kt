package com.kona.ktca.domain.port.inbound

import com.kona.ktca.domain.dto.TrafficZoneDTO
import com.kona.ktca.domain.model.TrafficZone

interface TrafficZoneCommandPort {

    suspend fun create(dto: TrafficZoneDTO): TrafficZone

    suspend fun update(zone: TrafficZone, dto: TrafficZoneDTO): TrafficZone

    suspend fun delete(zoneId: String)

    suspend fun validateTrafficZoneId(zoneId: String)

}