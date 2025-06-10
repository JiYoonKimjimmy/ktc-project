package com.kona.ktc.infrastructure.adapter.redis

import com.kona.common.infrastructure.cache.redis.RedisExecuteAdapter
import com.kona.common.infrastructure.enumerate.TrafficCacheKey
import com.kona.common.infrastructure.error.ErrorCode
import com.kona.common.infrastructure.error.exception.InternalServiceException
import com.kona.common.infrastructure.error.exception.ServiceUnavailableException
import com.kona.ktc.domain.model.Traffic
import com.kona.ktc.domain.model.TrafficWaiting
import com.kona.ktc.domain.port.outbound.TrafficControlPort
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.script.RedisScript
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class TrafficControlScriptExecuteAdapter(
    private val trafficControlScript: RedisScript<List<*>>,
    private val redisExecuteAdapter: RedisExecuteAdapter,
    @Value("\${ktc.traffic.control.defaultThreshold}")
    private val defaultThreshold: String
) : TrafficControlPort {

    override suspend fun controlTraffic(traffic: Traffic, now: Instant): TrafficWaiting {
        val script = trafficControlScript
        val zoneId = traffic.zoneId
        val token = traffic.token
        val nowMillis = now.toEpochMilli().toString()

        val keys = TrafficCacheKey.getTrafficControlKeys(zoneId).map { it.value }
        val args = listOf(token, nowMillis, defaultThreshold)

        val (result, number, estimatedTime, totalCount ) = redisExecuteAdapter.execute(script, keys, args).map { it as Long }

        return when (result) {
            -1L -> throw InternalServiceException(ErrorCode.TRAFFIC_ZONE_STATUS_IS_BLOCKED)
            -2L -> throw ServiceUnavailableException(ErrorCode.FAULTY_503_ERROR)
            else -> TrafficWaiting(result, number, estimatedTime, totalCount)
        }
    }

}