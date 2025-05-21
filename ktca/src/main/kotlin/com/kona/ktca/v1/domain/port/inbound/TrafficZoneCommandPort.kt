package com.kona.ktca.v1.domain.port.inbound

import com.kona.ktca.v1.application.dto.TrafficZoneDTO
import com.kona.ktca.v1.domain.model.TrafficZone

interface TrafficZoneCommandPort {

    suspend fun create(dto: TrafficZoneDTO): TrafficZone

    suspend fun update(dto: TrafficZoneDTO): TrafficZone

}