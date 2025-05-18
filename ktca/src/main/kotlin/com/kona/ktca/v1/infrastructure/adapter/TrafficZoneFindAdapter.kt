package com.kona.ktca.v1.infrastructure.adapter

import com.kona.common.infrastructure.cache.redis.RedisExecuteAdapter
import com.kona.common.infrastructure.enumerate.TrafficCacheKey.*
import com.kona.common.infrastructure.util.ONE_MINUTE_MILLIS
import com.kona.common.infrastructure.util.ZERO
import com.kona.common.infrastructure.util.ifNullOrMinus
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
        val zones = getTrafficZones(zoneId)
        val thresholds = getTrafficZoneThresholds(zones)
        return zones.map { TrafficZone(it, thresholds[it] ?: ZERO) }
    }

    private suspend fun getTrafficZones(zoneId: String?): List<String> {
        return redisExecuteAdapter.getValuesForSet(ACTIVATION_ZONES.key)
            .filter { if (zoneId != null) it == zoneId else true }
    }

    private suspend fun getTrafficZoneThresholds(zoneIds: List<String>): Map<String, Long> {
        return zoneIds.associateWith {
            redisExecuteAdapter.getValue(THRESHOLD.getKey(it))?.toLong().ifNullOrMinus(ZERO)
        }
    }

    override suspend fun findTrafficZoneWaiting(zoneId: String, threshold: Long): TrafficZoneWaiting {
        val queueKey = QUEUE.getKey(zoneId)
        val entyCountKey = ENTRY_COUNT.getKey(zoneId)

        val waitingCount = redisExecuteAdapter.getSizeForZSet(queueKey)
        val entryCount = redisExecuteAdapter.getValue(entyCountKey)?.toLong() ?: ZERO
        val estimatedClearTime = ceil(waitingCount.toDouble() / threshold).toLong() * ONE_MINUTE_MILLIS

        return TrafficZoneWaiting(
            waitingCount = waitingCount,
            entryCount = entryCount,
            estimatedClearTime = estimatedClearTime
        )
    }

}