package com.kona.ktca.domain.port.inbound

import com.kona.ktca.domain.dto.TrafficZoneGroupDTO
import com.kona.ktca.domain.model.TrafficZoneGroup

interface TrafficZoneGroupFindPort {

    suspend fun findTrafficZoneGroup(dto: TrafficZoneGroupDTO): TrafficZoneGroup

    suspend fun findAllTrafficZoneGroup(): List<TrafficZoneGroup>

}