package com.kona.ktca.domain.port.outbound

import com.kona.common.infrastructure.enumerate.TrafficZoneGroupStatus
import com.kona.ktca.domain.model.TrafficZoneGroup

interface TrafficZoneGroupRepository {

    suspend fun save(group: TrafficZoneGroup): TrafficZoneGroup

    suspend fun saveNextOrder(group: TrafficZoneGroup): TrafficZoneGroup

    suspend fun findByGroupId(groupId: String): TrafficZoneGroup?

    suspend fun findByGroupIdAndStatus(groupId: String, status: TrafficZoneGroupStatus): TrafficZoneGroup?

    suspend fun findAllByStatus(status: TrafficZoneGroupStatus): List<TrafficZoneGroup>

    suspend fun delete(groupId: String)

}