package com.kona.ktca.v1.infrastructure.repository

import com.kona.common.infrastructure.enumerate.TrafficZoneStatus
import com.kona.ktca.v1.infrastructure.repository.entity.TrafficZoneEntity

interface TrafficZoneRepository {

    suspend fun save(entity: TrafficZoneEntity): TrafficZoneEntity

    suspend fun findByZoneId(zoneId: String): TrafficZoneEntity?

    suspend fun findAllByStatus(status: TrafficZoneStatus): List<TrafficZoneEntity>

}