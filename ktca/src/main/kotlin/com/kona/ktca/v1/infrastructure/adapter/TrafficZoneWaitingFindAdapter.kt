package com.kona.ktca.v1.infrastructure.adapter

import com.kona.common.infrastructure.cache.redis.RedisExecuteAdapter
import com.kona.common.infrastructure.enumerate.TrafficCacheKey.ENTRY_COUNT
import com.kona.common.infrastructure.enumerate.TrafficCacheKey.QUEUE
import com.kona.common.infrastructure.util.ONE_MINUTE_MILLIS
import com.kona.common.infrastructure.util.ZERO
import com.kona.ktca.v1.domain.model.TrafficZoneWaiting
import com.kona.ktca.v1.domain.port.outbound.TrafficZoneWaitingFindPort
import org.springframework.stereotype.Component
import kotlin.math.ceil

@Component
class TrafficZoneWaitingFindAdapter(
    private val redisExecuteAdapter: RedisExecuteAdapter
) : TrafficZoneWaitingFindPort {

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