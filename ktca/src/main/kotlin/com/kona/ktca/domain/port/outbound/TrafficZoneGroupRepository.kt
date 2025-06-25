package com.kona.ktca.domain.port.outbound

import com.kona.common.infrastructure.enumerate.TrafficZoneGroupStatus
import com.kona.ktca.domain.model.TrafficZoneGroup

interface TrafficZoneGroupRepository {

    suspend fun save(group: TrafficZoneGroup): TrafficZoneGroup

    suspend fun saveNextOrder(name: String): TrafficZoneGroup

    suspend fun findByGroupId(groupId: Long): TrafficZoneGroup?

    suspend fun findByGroupIdAndStatus(groupId: Long, status: TrafficZoneGroupStatus): TrafficZoneGroup?

    suspend fun findAllByStatus(status: TrafficZoneGroupStatus): List<TrafficZoneGroup>

    suspend fun delete(groupId: Long)

}