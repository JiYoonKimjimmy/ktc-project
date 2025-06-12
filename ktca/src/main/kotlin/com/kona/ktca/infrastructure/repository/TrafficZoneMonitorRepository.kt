package com.kona.ktca.infrastructure.repository

import com.kona.ktca.infrastructure.repository.entity.TrafficZoneMonitorEntity

interface TrafficZoneMonitorRepository {

    suspend fun saveAll(entities: List<TrafficZoneMonitorEntity>): List<TrafficZoneMonitorEntity>

}