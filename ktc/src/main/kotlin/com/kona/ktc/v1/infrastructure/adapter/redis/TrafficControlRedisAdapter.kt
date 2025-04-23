package com.kona.ktc.v1.infrastructure.adapter.redis

import com.kona.ktc.v1.domain.model.TrafficToken
import com.kona.ktc.v1.domain.model.TrafficWaiting
import com.kona.ktc.v1.domain.port.outbound.TrafficControlPort
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.ReactiveStringRedisTemplate
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
class TrafficControlRedisAdapter(
    private val trafficControlRedisScript: TrafficControlRedisScript,
    private val reactiveStringRedisTemplate: ReactiveStringRedisTemplate,

    @Value("\${ktc.traffic.control.defaultThreshold}")
    private val defaultThreshold: Long

) : TrafficControlPort {

    override suspend fun controlTraffic(token: TrafficToken): TrafficWaiting {
        val script = trafficControlRedisScript.getScript()
        val now = Instant.now().epochSecond
        val score = now * 1000 + (now % 1000)

        val baseKey = "ktc:{${token.zoneId}}"
        val keys = listOf(
            "$baseKey:zqueue",
            "$baseKey:tokens",
            "$baseKey:last_refill_ts",
            "$baseKey:threshold"
        )

        val args = listOf(
            token.token,
            score.toString(),
            now.toString(),
            defaultThreshold.toString()
        )

        val result = reactiveStringRedisTemplate.execute(script, keys, *args.toTypedArray()).awaitSingle()
        return TrafficWaiting(
            number = result[0] as Long,
            estimatedTime = result[1] as Long,
            totalCount = result[2] as Long
        )
    }

} 