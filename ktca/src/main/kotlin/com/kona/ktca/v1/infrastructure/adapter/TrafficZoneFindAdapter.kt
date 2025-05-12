package com.kona.ktca.v1.infrastructure.adapter

import com.kona.common.infrastructure.cache.redis.RedisExecuteAdapter
import com.kona.common.infrastructure.enumerate.TrafficCacheKey.*
import com.kona.common.infrastructure.util.*
import com.kona.ktca.v1.domain.model.TrafficZone
import com.kona.ktca.v1.domain.model.TrafficZoneWaiting
import com.kona.ktca.v1.domain.port.outbound.TrafficZoneFindPort
import org.springframework.stereotype.Component
import kotlin.math.ceil

@Component
class TrafficZoneFindAdapter(
    private val redisExecuteAdapter: RedisExecuteAdapter,
) : TrafficZoneFindPort {

    override suspend fun findAllTrafficZone(zoneId: String?): List<TrafficZone> {
        val zoneIds = getTrafficZoneIds(zoneId)
        val thresholds = getTrafficZoneThresholds(zoneIds)
        return zoneIds.map { TrafficZone(it, thresholds[it] ?: ZERO) }
    }

    private suspend fun getTrafficZoneIds(zoneId: String?): List<String> {
        return redisExecuteAdapter.keys(ZQUEUE_KEY_PATTERN)
            .map { it.getZoneId() }
            .filter { if (zoneId != null) it == zoneId else true }
    }

    private suspend fun getTrafficZoneThresholds(zoneIds: List<String>): Map<String, Long> {
        return zoneIds.associateWith {
            redisExecuteAdapter.getValue(TRAFFIC_THRESHOLD.getKey(it))?.toLong().ifNullOrMinus(ZERO)
        }
    }

    override suspend fun findAllTrafficZoneWaiting(zones: List<TrafficZone>): List<TrafficZone> {
        return zones.map {
            it.applyWaiting(findTrafficZoneWaiting(it.zoneId, it.threshold))
        }
    }

    private suspend fun findTrafficZoneWaiting(zoneId: String, threshold: Long): TrafficZoneWaiting {
        val zqueueSize = redisExecuteAdapter.getZSetSize(TRAFFIC_ZQUEUE.getKey(zoneId))
        val entryCount = redisExecuteAdapter.getValue(TRAFFIC_ENTRY_COUNTER.getKey(zoneId))?.toLong().ifNullOrMinus(ZERO)
        val estimatedClearTime = ceil(zqueueSize.toDouble() / threshold).toLong() * ONE_MINUTE_MILLE

        return TrafficZoneWaiting(
            waitingCount = zqueueSize,
            entryCount = entryCount,
            estimatedClearTime = estimatedClearTime
        )
    }

}