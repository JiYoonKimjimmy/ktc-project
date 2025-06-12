package com.kona.ktca.infrastructure.cache

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import com.kona.common.infrastructure.util.convertPatternOf
import com.kona.ktca.domain.model.TrafficZoneMonitor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.LocalDate

@Component
class TrafficZoneMonitorCacheAdapterImpl : TrafficZoneMonitorCacheAdapter {

    private val trafficZoneMonitoringCache: Cache<String, List<TrafficZoneMonitor>> = Caffeine.newBuilder()
        .maximumSize(1000)
        .expireAfterWrite(Duration.ofDays(1))
        .build()

    private val trafficZoneWaitingCountCache: Cache<String, Int> = Caffeine.newBuilder()
        .maximumSize(1000)
        .expireAfterWrite(Duration.ofDays(1))
        .build()

    override suspend fun saveLatestTrafficZoneMonitoring(
        monitoring: List<TrafficZoneMonitor>,
        now: LocalDate
    ): List<TrafficZoneMonitor> = withContext(Dispatchers.IO) {
        trafficZoneMonitoringCache.put(now.convertPatternOf(), monitoring)
        monitoring
    }

    override suspend fun findLatestTrafficZoneMonitoring(now: LocalDate): List<TrafficZoneMonitor> = withContext(Dispatchers.IO) {
        trafficZoneMonitoringCache.getIfPresent(now.convertPatternOf()) ?: emptyList()
    }

    override suspend fun deleteTrafficZoneMonitoring(now: LocalDate) = withContext(Dispatchers.IO) {
        trafficZoneMonitoringCache.invalidate(now.convertPatternOf())
    }

    override suspend fun clearTrafficZoneMonitoring() = withContext(Dispatchers.IO) {
        trafficZoneMonitoringCache.invalidateAll()
    }

    override suspend fun updateTrafficZoneWaitingCount(zoneId: String, isZero: Boolean): Int = withContext(Dispatchers.IO) {
        val current = trafficZoneWaitingCountCache.getIfPresent(zoneId) ?: 0
        val updated = if (isZero) current + 1 else 0
        trafficZoneWaitingCountCache.put(zoneId, updated)
        updated
    }

    override suspend fun findTrafficZoneWaitingCount(zoneId: String): Int = withContext(Dispatchers.IO) {
        trafficZoneWaitingCountCache.getIfPresent(zoneId) ?: 0
    }

}