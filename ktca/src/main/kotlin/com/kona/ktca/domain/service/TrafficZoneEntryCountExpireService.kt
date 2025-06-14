package com.kona.ktca.domain.service

import com.kona.common.infrastructure.cache.redis.RedisExecuteAdapter
import com.kona.common.infrastructure.enumerate.TrafficCacheKey.ENTRY_COUNT
import com.kona.ktca.domain.port.inbound.TrafficZoneEntryCountExpirePort
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.LocalDateTime

@Service
class TrafficZoneEntryCountExpireService(
    private val redisExecuteAdapter: RedisExecuteAdapter
) : TrafficZoneEntryCountExpirePort {

    override suspend fun expireTrafficZoneEntryCount(zoneIds: List<String>): Int {
        /**
         * [트래픽 Zone `entry-count` Cache 만료 처리]
         * 1. 현재 시간 기준 다음 일자 00:00 자정 시간 확인
         * 2. 현재 시간 부터 자정 시간까지 `entry-count` Cache TTL 적용
         */
        val now = LocalDateTime.now()
        val tomorrowMidnight = now.toLocalDate().plusDays(1).atStartOfDay()
        val duration = Duration.between(now, tomorrowMidnight)
        return zoneIds.count {
            val entryCountKey = ENTRY_COUNT.getKey(it)
            redisExecuteAdapter.expire(entryCountKey, duration)
        }
    }

}