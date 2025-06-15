package com.kona.ktca.infrastructure.adapter

import com.kona.common.infrastructure.cache.redis.RedisExecuteAdapter
import com.kona.common.infrastructure.enumerate.TrafficCacheKey
import com.kona.ktca.domain.port.outbound.TrafficZoneCachingPort
import org.springframework.stereotype.Component

@Component
class TrafficZoneCachingAdapter(
    private val redisExecuteAdapter: RedisExecuteAdapter
) : TrafficZoneCachingPort {

    override suspend fun clearAll(zoneIds: List<String>) {
        val activeZoneIds = zoneIds.takeIf { it.isNotEmpty() } ?: redisExecuteAdapter.getValuesForSet(TrafficCacheKey.ACTIVATION_ZONES.key)
        activeZoneIds
            .associateWith { TrafficCacheKey.getTrafficControlKeys(it).values.toList() }
            .values.forEach { redisExecuteAdapter.deleteAll(it) }
    }

}