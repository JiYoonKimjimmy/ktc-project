package com.kona.ktca.infrastructure.adapter

import com.kona.common.infrastructure.cache.redis.RedisExecuteAdapter
import com.kona.common.infrastructure.enumerate.TrafficCacheKey.*
import com.kona.ktca.domain.port.outbound.TrafficTokenExpireExecutePort
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.script.RedisScript
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class TrafficTokenExpireScriptExecuteAdapter(
    private val trafficExpireScript: RedisScript<List<*>>,
    private val redisExecuteAdapter: RedisExecuteAdapter,
) : TrafficTokenExpireExecutePort {
    // logger
    private val logger = LoggerFactory.getLogger(this::class.java)

    override suspend fun expireTrafficToken(now: Instant): Long {
        val script = trafficExpireScript

        // 모든 zqueue key 목록 조회
        val activeZoneIds = redisExecuteAdapter.getValuesForSet(ACTIVATION_ZONES.key)
        val nowMilli = now.toEpochMilli().toString()
        logger.info("Execute expire-traffic script. [Zone count: ${activeZoneIds.size}, nowMilli: $nowMilli]")

        return activeZoneIds.sumOf {
            val queueKey = QUEUE.getKey(it)
            val tokenLastPollingTimeKey = TOKEN_LAST_POLLING_TIME.getKey(it)
            val keys = listOf(queueKey, tokenLastPollingTimeKey)
            val args = listOf(nowMilli)

            redisExecuteAdapter.execute(script, keys, args)[0] as Long
        }
    }

}