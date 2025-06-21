package com.kona.ktca.infrastructure.adapter

import com.kona.common.infrastructure.cache.redis.RedisExecuteAdapter
import com.kona.common.infrastructure.enumerate.TrafficCacheKey
import com.kona.common.infrastructure.enumerate.TrafficCacheKey.ACTIVATION_ZONES
import com.kona.common.infrastructure.enumerate.TrafficCacheKey.ENTRY_COUNT
import com.kona.common.infrastructure.enumerate.TrafficCacheKey.QUEUE
import com.kona.common.infrastructure.enumerate.TrafficCacheKey.QUEUE_STATUS
import com.kona.common.infrastructure.enumerate.TrafficCacheKey.THRESHOLD
import com.kona.common.infrastructure.enumerate.TrafficZoneStatus.ACTIVE
import com.kona.common.infrastructure.enumerate.TrafficZoneStatus.BLOCKED
import com.kona.common.infrastructure.enumerate.TrafficZoneStatus.DELETED
import com.kona.common.infrastructure.enumerate.TrafficZoneStatus.FAULTY_503
import com.kona.common.infrastructure.util.ONE_MINUTE_MILLIS
import com.kona.common.infrastructure.util.QUEUE_ACTIVATION_TIME_KEY
import com.kona.common.infrastructure.util.QUEUE_STATUS_KEY
import com.kona.common.infrastructure.util.ZERO
import com.kona.common.infrastructure.util.convertUTCEpochTime
import com.kona.ktca.domain.model.TrafficZone
import com.kona.ktca.domain.model.TrafficZoneWaiting
import com.kona.ktca.domain.port.outbound.TrafficZoneCachingPort
import org.springframework.stereotype.Component
import kotlin.math.ceil

@Component
class TrafficZoneCachingAdapter(
    private val redisExecuteAdapter: RedisExecuteAdapter
) : TrafficZoneCachingPort {

    override suspend fun save(zone: TrafficZone): TrafficZone {
        val zoneId = zone.zoneId
        val threshold = zone.threshold.toString()
        val queueStatus = zone.status.name
        val queueActivationTime = zone.activationTime.convertUTCEpochTime()
        val zoneStatus = mapOf(
            QUEUE_STATUS_KEY to queueStatus,
            QUEUE_ACTIVATION_TIME_KEY to queueActivationTime
        )

        redisExecuteAdapter.setValue(THRESHOLD.getKey(zoneId), threshold)
        redisExecuteAdapter.pushHashMap(QUEUE_STATUS.getKey(zoneId), zoneStatus)

        when (zone.status) {
            ACTIVE -> redisExecuteAdapter.addValueForSet(ACTIVATION_ZONES.key, zoneId)
            BLOCKED, DELETED, FAULTY_503 -> redisExecuteAdapter.removeValueForSet(ACTIVATION_ZONES.key, zoneId)
        }

        return zone
    }

    override suspend fun findTrafficZoneWaiting(zone: TrafficZone): TrafficZone {
        val zoneId = zone.zoneId
        val threshold = zone.threshold
        val queueKey = QUEUE.getKey(zoneId)
        val entyCountKey = ENTRY_COUNT.getKey(zoneId)

        val waitingCount = redisExecuteAdapter.getSizeForZSet(queueKey)
        val entryCount = redisExecuteAdapter.getValue(entyCountKey)?.toLong() ?: ZERO
        val estimatedClearTime = ceil(waitingCount.toDouble() / threshold).toLong() * ONE_MINUTE_MILLIS
        val waiting = TrafficZoneWaiting(
            waitingCount = waitingCount,
            entryCount = entryCount,
            estimatedClearTime = estimatedClearTime
        )

        return zone.applyWaiting(waiting)
    }

    override suspend fun clearAll(zoneIds: List<String>) {
        val activeZoneIds = zoneIds.takeIf { it.isNotEmpty() } ?: redisExecuteAdapter.getValuesForSet(ACTIVATION_ZONES.key)
        activeZoneIds
            .associateWith { TrafficCacheKey.getTrafficControlKeys(it).values.toList() }
            .values
            .forEach { redisExecuteAdapter.deleteAll(it) }
    }

}