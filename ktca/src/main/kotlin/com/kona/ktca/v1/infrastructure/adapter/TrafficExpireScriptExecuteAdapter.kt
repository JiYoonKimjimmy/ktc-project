package com.kona.ktca.v1.infrastructure.adapter

import com.kona.common.infrastructure.cache.redis.RedisExecuteAdapter
import com.kona.common.infrastructure.enumerate.TrafficCacheKey.*
import com.kona.common.infrastructure.util.toInstantEpochMilli
import com.kona.ktca.v1.domain.port.outbound.TrafficExpireExecutePort
import com.kona.ktca.v1.infrastructure.redis.TrafficExpireRedisScript
import jakarta.annotation.PostConstruct
import org.springframework.data.redis.core.script.RedisScript
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class TrafficExpireScriptExecuteAdapter(
    private val trafficExpireRedisScript: TrafficExpireRedisScript,
    private val redisExecuteAdapter: RedisExecuteAdapter,
) : TrafficExpireExecutePort {

    private lateinit var script: RedisScript<List<*>>

    @PostConstruct
    fun init() {
        script = trafficExpireRedisScript.getScript()
    }

    override suspend fun execute(): Long {
        // 모든 zqueue key 목록 조회
        return redisExecuteAdapter.getValuesForSet(ACTIVATION_ZONES.key)
            // 각 zone 별 토큰 만료 처리
            .sumOf { expireTraffic(it) }
    }

    private suspend fun expireTraffic(zoneId: String): Long {
        // zoneId 추출
        val queueKey = QUEUE.getKey(zoneId)
        val expirationTime = Instant.now().minusSeconds(180).toInstantEpochMilli()
        val keys = listOf(queueKey)
        val args = listOf(expirationTime)

        // traffic-expire script 실행
        val result = redisExecuteAdapter.execute(script, keys, args).first() as Long?

        return result ?: 0
    }

}