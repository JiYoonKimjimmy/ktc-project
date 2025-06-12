package com.kona.ktca.infrastructure.adapter

import com.kona.common.infrastructure.cache.redis.RedisExecuteAdapter
import com.kona.common.infrastructure.enumerate.TrafficCacheKey.*
import com.kona.ktca.domain.port.outbound.TrafficTokenExpireExecutePort
import org.springframework.data.redis.core.script.RedisScript
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class TrafficTokenExpireScriptExecuteAdapter(
    private val trafficExpireScript: RedisScript<List<*>>,
    private val redisExecuteAdapter: RedisExecuteAdapter,
) : TrafficTokenExpireExecutePort {

    override suspend fun expireTrafficToken(now: Instant): Long {
        val script = trafficExpireScript

        // 모든 zqueue key 목록 조회
        val activeZoneIds = redisExecuteAdapter.getValuesForSet(ACTIVATION_ZONES.key)

        return activeZoneIds.sumOf {
            val queueKey = QUEUE.getKey(it)
            val tokenLastPollingTimeKey = TOKEN_LAST_POLLING_TIME.getKey(it)
            val keys = listOf(queueKey, tokenLastPollingTimeKey)
            val args = listOf(now.toEpochMilli().toString())

            redisExecuteAdapter.execute(script, keys, args)[0] as Long
        }
    }

}