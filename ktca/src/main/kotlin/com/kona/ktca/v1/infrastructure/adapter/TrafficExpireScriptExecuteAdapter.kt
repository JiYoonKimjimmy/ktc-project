package com.kona.ktca.v1.infrastructure.adapter

import com.kona.common.infrastructure.cache.redis.RedisExecuteAdapter
import com.kona.common.infrastructure.enumerate.TrafficCacheKey.TRAFFIC_LAST_ENTRY_TIME
import com.kona.ktca.v1.domain.port.outbound.TrafficExpireExecutePort
import com.kona.ktca.v1.infrastructure.redis.TrafficExpireRedisScript
import jakarta.annotation.PostConstruct
import org.springframework.data.redis.core.script.RedisScript
import org.springframework.stereotype.Component

@Component
class TrafficExpireScriptExecuteAdapter(
    private val trafficExpireRedisScript: TrafficExpireRedisScript,
    private val redisExecuteAdapter: RedisExecuteAdapter,
) : TrafficExpireExecutePort {

    companion object {
        const val ZQUEUE_KEY_PATTERN = "ktc:*:zqueue"
        const val ZQUEUE_KEY_PREFIX = "ktc:{"
        const val ZQUEUE_KEY_SUFFIX = "}:zqueue"
    }

    private lateinit var script: RedisScript<List<*>>

    @PostConstruct
    fun init() {
        script = trafficExpireRedisScript.getScript()
    }

    override suspend fun execute(): Long {
        // 모든 zqueue key 목록 조회
        return redisExecuteAdapter.keys(ZQUEUE_KEY_PATTERN)
            // 각 zone 별 토큰 만료 처리
            .sumOf { expireTraffic(it) }
    }

    private suspend fun expireTraffic(zqueueKey: String): Long {
        // zoneId 추출
        val zoneId = zqueueKey.substringAfter(ZQUEUE_KEY_PREFIX).substringBefore(ZQUEUE_KEY_SUFFIX)
        val lastEntryKey = TRAFFIC_LAST_ENTRY_TIME.getKey(zoneId)
        val keys = listOf(zqueueKey, lastEntryKey)

        // traffic-expire script 실행
        val result = redisExecuteAdapter.execute(script, keys, emptyList()).first() as Long?

        return result ?: 0
    }

}