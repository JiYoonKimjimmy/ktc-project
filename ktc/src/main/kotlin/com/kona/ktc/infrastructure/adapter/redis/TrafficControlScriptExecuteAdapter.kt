package com.kona.ktc.infrastructure.adapter.redis

import com.kona.common.infrastructure.cache.redis.RedisExecuteAdapter
import com.kona.common.infrastructure.enumerate.TrafficCacheKey
import com.kona.common.infrastructure.util.toTokenScore
import com.kona.ktc.domain.model.Traffic
import com.kona.ktc.domain.model.TrafficWaiting
import com.kona.ktc.domain.port.outbound.TrafficControlPort
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class TrafficControlScriptExecuteAdapter(

    private val trafficControlScript: TrafficControlScript,
    private val redisExecuteAdapter: RedisExecuteAdapter,

    @Value("\${ktc.traffic.control.defaultThreshold}")
    private val defaultThreshold: String
    
) : TrafficControlPort {

    override suspend fun controlTraffic(traffic: Traffic, now: Instant): TrafficWaiting {
        val script = trafficControlScript.getScript()
        val zoneId = traffic.zoneId
        val token = traffic.token
        val score = now.toTokenScore().toString()
        val nowMillis = now.toEpochMilli().toString()

        val keys = TrafficCacheKey.getTrafficControlKeys(zoneId).map { it.value }
        val args = listOf(token, score, nowMillis, defaultThreshold)

        val result = redisExecuteAdapter.execute(script, keys, args).map { it as Long }

        return TrafficWaiting(result)
    }

}