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
import com.kona.common.infrastructure.error.exception.InternalServiceException
import com.kona.common.infrastructure.error.ErrorCode

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

        if (result[0] == -1L) throw InternalServiceException(ErrorCode.TRAFFIC_ZONE_STATUS_IS_BLOCKED)

        return TrafficWaiting(result)
    }

}