package com.kona.ktca.infrastructure.cache

import com.kona.ktca.domain.model.TrafficZoneMonitor
import java.time.LocalDate

interface TrafficZoneMonitorCacheAdapter {

    suspend fun saveLatestTrafficZoneMonitoring(monitoring: List<TrafficZoneMonitor>, now: LocalDate = LocalDate.now()): List<TrafficZoneMonitor>

    suspend fun findLatestTrafficZoneMonitoring(now: LocalDate = LocalDate.now()): List<TrafficZoneMonitor>

    suspend fun deleteTrafficZoneMonitoring(now: LocalDate = LocalDate.now())

    suspend fun clearTrafficZoneMonitoring()

    suspend fun updateTrafficZoneWaitingCount(zoneId: String, isZero: Boolean): Int

    suspend fun findTrafficZoneWaitingCount(zoneId: String): Int

}