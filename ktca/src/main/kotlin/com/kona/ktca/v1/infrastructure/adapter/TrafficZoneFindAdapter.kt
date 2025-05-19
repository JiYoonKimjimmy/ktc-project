package com.kona.ktca.v1.infrastructure.adapter

import com.kona.common.infrastructure.cache.redis.RedisExecuteAdapter
import com.kona.common.infrastructure.enumerate.TrafficCacheKey.ACTIVATION_ZONES
import com.kona.common.infrastructure.enumerate.TrafficCacheKey.THRESHOLD
import com.kona.common.infrastructure.util.ZERO
import com.kona.common.infrastructure.util.ifNullOrMinus
import com.kona.ktca.v1.domain.model.TrafficZone
import com.kona.ktca.v1.domain.port.outbound.TrafficZoneFindPort
import org.springframework.stereotype.Component

@Component
class TrafficZoneFindAdapter(
    private val redisExecuteAdapter: RedisExecuteAdapter
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

}