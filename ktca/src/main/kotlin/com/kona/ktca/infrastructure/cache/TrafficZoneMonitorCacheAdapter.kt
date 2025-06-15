package com.kona.ktca.infrastructure.cache

import com.kona.ktca.domain.model.TrafficZoneMonitor
import java.time.LocalDate

interface TrafficZoneMonitorCacheAdapter {

    suspend fun saveMonitoringLatestResult(monitoring: List<TrafficZoneMonitor>, now: LocalDate = LocalDate.now()): List<TrafficZoneMonitor>

    suspend fun findAllMonitoringLatestResult(now: LocalDate = LocalDate.now()): List<TrafficZoneMonitor>

    suspend fun deleteMonitoringLatestResult(now: LocalDate = LocalDate.now())

    suspend fun clearMonitoringLatestResult()

    suspend fun incrementMonitoringStopCounter(zoneId: String, isZero: Boolean): Int

    suspend fun findMonitoringStopCounter(zoneId: String): Int

}