package com.kona.ktca.v1.infrastructure.adapter.redis

import com.kona.common.enumerate.TrafficCacheKey.TRAFFIC_LAST_ENTRY_TIME
import com.kona.common.infrastructure.redis.RedisExecuteAdapter
import com.kona.ktca.v1.domain.port.outbound.TrafficExpirePort
import jakarta.annotation.PostConstruct
import org.springframework.data.redis.core.script.RedisScript
import org.springframework.stereotype.Component

@Component
class TrafficExpireScriptExecuteAdapter(
    private val trafficExpireScript: TrafficExpireScript,
    private val redisExecuteAdapter: RedisExecuteAdapter,
) : TrafficExpirePort {

    companion object {
        const val ZQUEUE_KEY_PATTERN = "ktc:*:zqueue"
        const val ZQUEUE_KEY_PREFIX = "ktc:{"
        const val ZQUEUE_KEY_SUFFIX = "}:zqueue"
    }

    private lateinit var script: RedisScript<List<*>>

    @PostConstruct
    fun init() {
        script = trafficExpireScript.getScript()
    }

    override suspend fun expireTraffic(): Long {
        // 모든 zqueue key 목록 조회
        return redisExecuteAdapter.keys(ZQUEUE_KEY_PATTERN)
            // 각 zone 별 토큰 만료 처리
            .sumOf { expireTraffic(it) }
    }

    private suspend fun expireTraffic(zqueueKey: String): Long {
        // zoneId 추출
        val zoneId = zqueueKey.substringAfter(ZQUEUE_KEY_PREFIX).substringBefore(ZQUEUE_KEY_SUFFIX)
        val lastEntryKey = TRAFFIC_LAST_ENTRY_TIME.getKey(zoneId)

        // traffic-expire script 실행
        val keys = listOf(zqueueKey, lastEntryKey)
        return redisExecuteAdapter.execute(script, keys, emptyList())[0] as Long
    }

}