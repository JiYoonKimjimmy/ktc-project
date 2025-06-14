package com.kona.ktca.infrastructure.adapter

import com.kona.common.infrastructure.cache.redis.RedisExecuteAdapter
import com.kona.ktca.domain.model.TrafficZoneMonitor
import com.kona.ktca.domain.port.outbound.TrafficZoneMonitorSavePort
import com.kona.ktca.infrastructure.cache.TrafficZoneMonitorCacheAdapter
import com.kona.ktca.infrastructure.repository.TrafficZoneMonitorRepository
import com.kona.ktca.infrastructure.repository.entity.TrafficZoneMonitorEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Component

@Component
class TrafficZoneMonitorSaveAdapter(
    private val trafficZoneMonitorRepository: TrafficZoneMonitorRepository,
    private val trafficZoneMonitorCacheAdapter: TrafficZoneMonitorCacheAdapter,
    private val redisExecuteAdapter: RedisExecuteAdapter
) : TrafficZoneMonitorSavePort {

    override suspend fun saveAll(monitoring: List<TrafficZoneMonitor>): List<TrafficZoneMonitor> = withContext(Dispatchers.IO) {
        /**
         * [Zone 별 waitingCount 0 연속 횟수 확인]
         * - saveOnlyCache: 모니터링 조회 결과 `waitingCount: 0` 결과 5회 이상인 경우, Cache 만 저장
         * - saveDBAndCache: 모니터링 조회 결과 `waitingCount: 0` 결과 5회 이하인 경우, DB 저장 후 Cache 저장
         */
        val (saveOnlyCache, saveDBAndCache) = monitoring.partition { it.checkMonitoringStopCounter() }

        // Zone 모니터링 결과 DB 저장
        val saved = saveDBAndCache.saveEntities()

        // Zone 모니터링 결과 Cache 저장
        (saved + saveOnlyCache).saveCaches()
    }

    private suspend fun TrafficZoneMonitor.checkMonitoringStopCounter(): Boolean {
        val counter = trafficZoneMonitorCacheAdapter.incrementMonitoringStopCounter(zoneId = zoneId, isZero = (waitingCount == 0L))
        return counter > 5
    }

    private suspend fun List<TrafficZoneMonitor>.saveEntities(): List<TrafficZoneMonitor> {
        return if (this.isNotEmpty()) {
            this.map { TrafficZoneMonitorEntity.of(domain = it) }
                .let { trafficZoneMonitorRepository.saveAll(entities = it) }
                .map { it.toDomain()}
        } else {
            emptyList()
        }
    }

    private suspend fun List<TrafficZoneMonitor>.saveCaches(): List<TrafficZoneMonitor> {
        return if (this.isNotEmpty()) {
            trafficZoneMonitorCacheAdapter.saveMonitoringLatestResult(monitoring = this)
        } else {
            emptyList()
        }
    }

}