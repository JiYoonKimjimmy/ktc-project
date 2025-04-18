package com.kona.ktc.v0.infrastructure.redis

import com.kona.ktc.v0.domain.model.TrafficToken
import com.kona.ktc.v0.domain.model.TrafficWaiting
import com.kona.ktc.v0.domain.repository.TrafficRepository
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
class TrafficRedisScriptRepository(
    private val stringRedisTemplate: StringRedisTemplate,
    private val trafficRedisScript: TrafficRedisScript
) : TrafficRepository {

    override suspend fun controlTraffic(token: TrafficToken): TrafficWaiting {
        val now = Instant.now().epochSecond
        val score = now * 1000 + (now % 1000)

        val result = stringRedisTemplate.execute(
            trafficRedisScript.getScript(),
            listOf(),
            token.zoneId,
            token.token,
            score.toString(),
            now.toString()
        ) as List<*>

        return TrafficWaiting(
            number = result[0] as Long,
            estimatedTime = result[1] as Long,
            totalCount = result[2] as Long
        )
    }
} 