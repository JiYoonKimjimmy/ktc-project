package com.kona.ktc.v1.infrastructure.adapter.redis

import com.kona.ktc.v1.domain.model.TrafficToken
import com.kona.ktc.v1.domain.model.TrafficWaiting
import com.kona.ktc.v1.domain.port.outbound.TrafficControlPort
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
class TrafficControlRedisAdapter(
    private val trafficControlRedisScript: TrafficControlRedisScript,
    private val stringRedisTemplate: StringRedisTemplate
) : TrafficControlPort {

    override suspend fun controlTraffic(token: TrafficToken): TrafficWaiting {
        val script = trafficControlRedisScript.getScript()
        val now = Instant.now().epochSecond
        val score = now * 1000 + (now % 1000)

        val result = stringRedisTemplate.execute(
            script,
            emptyList(),
            token.zoneId,
            token.token,
            score.toString(),
            now.toString()
        )

        return TrafficWaiting(
            number = result[0] as Long,
            estimatedTime = result[1] as Long,
            totalCount = result[2] as Long
        )
    }

} 