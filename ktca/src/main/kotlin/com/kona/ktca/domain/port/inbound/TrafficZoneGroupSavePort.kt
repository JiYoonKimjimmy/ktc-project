package com.kona.ktca.domain.port.inbound

import com.kona.ktca.domain.dto.TrafficZoneGroupDTO
import com.kona.ktca.domain.model.TrafficZoneGroup

interface TrafficZoneGroupSavePort {

    suspend fun create(name: String): TrafficZoneGroup

    suspend fun update(dto: TrafficZoneGroupDTO): TrafficZoneGroup

}