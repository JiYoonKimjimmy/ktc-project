package com.kona.ktca.infrastructure.adapter

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import com.kona.common.infrastructure.util.convertPatternOf
import com.kona.ktca.domain.model.TrafficZoneMonitor
import com.kona.ktca.domain.port.outbound.TrafficZoneMonitorCachingPort
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.LocalDate

@Component
class TrafficZoneMonitorCachingAdapter : TrafficZoneMonitorCachingPort {

    private val monitoringLatestResultCache: Cache<String, List<TrafficZoneMonitor>> = Caffeine.newBuilder()
        .maximumSize(1000)
        .expireAfterWrite(Duration.ofDays(1))
        .build()

    private val monitoringStopCounterCache: Cache<String, Int> = Caffeine.newBuilder()
        .maximumSize(1000)
        .expireAfterWrite(Duration.ofDays(1))
        .build()

    override suspend fun saveMonitoringLatestResult(
        monitoring: List<TrafficZoneMonitor>,
        now: LocalDate
    ): List<TrafficZoneMonitor> = withContext(Dispatchers.IO) {
        monitoringLatestResultCache.put(now.convertPatternOf(), monitoring)
        monitoring
    }

    override suspend fun findAllMonitoringLatestResult(now: LocalDate): List<TrafficZoneMonitor> = withContext(Dispatchers.IO) {
        monitoringLatestResultCache.getIfPresent(now.convertPatternOf()) ?: emptyList()
    }

    override suspend fun deleteMonitoringLatestResult(now: LocalDate) = withContext(Dispatchers.IO) {
        monitoringLatestResultCache.invalidate(now.convertPatternOf())
    }

    override suspend fun clearMonitoringLatestResult() = withContext(Dispatchers.IO) {
        monitoringLatestResultCache.invalidateAll()
    }

    override suspend fun incrementMonitoringStopCounter(zoneId: String, isZero: Boolean): Int = withContext(Dispatchers.IO) {
        val current = monitoringStopCounterCache.getIfPresent(zoneId) ?: 0
        val updated = if (isZero) current + 1 else 0
        monitoringStopCounterCache.put(zoneId, updated)
        updated
    }

    override suspend fun findMonitoringStopCounter(zoneId: String): Int = withContext(Dispatchers.IO) {
        monitoringStopCounterCache.getIfPresent(zoneId) ?: 0
    }

}