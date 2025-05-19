package com.kona.ktca.v1.domain.port.inbound

import com.kona.ktca.v1.domain.model.TrafficZone
import com.kona.ktca.v1.domain.port.dto.TrafficZoneDTO

interface TrafficZoneManagePort {

    suspend fun save(dto: TrafficZoneDTO): TrafficZone

}