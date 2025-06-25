package com.kona.ktca.domain.port.inbound

import com.kona.ktca.domain.model.TrafficZoneGroup

interface TrafficZoneGroupFindPort {

    suspend fun findTrafficZoneGroup(groupId: Long): TrafficZoneGroup

    suspend fun findAllTrafficZoneGroup(): List<TrafficZoneGroup>

}