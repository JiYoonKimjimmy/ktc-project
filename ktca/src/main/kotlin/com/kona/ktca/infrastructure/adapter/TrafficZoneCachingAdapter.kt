package com.kona.ktca.infrastructure.adapter

import com.kona.common.infrastructure.cache.redis.RedisExecuteAdapter
import com.kona.common.infrastructure.enumerate.TrafficCacheKey
import com.kona.ktca.domain.port.outbound.TrafficZoneCachingPort
import org.springframework.stereotype.Component

@Component
class TrafficZoneCachingAdapter(
    private val redisExecuteAdapter: RedisExecuteAdapter
) : TrafficZoneCachingPort {

    override suspend fun clear(zoneIds: List<String>) {
        val activeZoneIds = zoneIds.takeIf { it.isNotEmpty() } ?: redisExecuteAdapter.getValuesForSet(TrafficCacheKey.ACTIVATION_ZONES.key)
        val keys = activeZoneIds.flatMap { TrafficCacheKey.getTrafficControlKeys(it).values }
        redisExecuteAdapter.deleteAll(keys)
    }

}