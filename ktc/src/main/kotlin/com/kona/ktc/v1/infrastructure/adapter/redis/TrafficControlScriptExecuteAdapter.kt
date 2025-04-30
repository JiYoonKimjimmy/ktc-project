package com.kona.ktc.v1.infrastructure.adapter.redis

import com.kona.common.enumerate.TrafficCacheKey
import com.kona.common.infrastructure.redis.RedisExecuteAdapter
import com.kona.ktc.v1.domain.model.TrafficToken
import com.kona.ktc.v1.domain.model.TrafficWaiting
import com.kona.ktc.v1.domain.port.outbound.TrafficControlPort
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
class TrafficControlScriptExecuteAdapter(

    private val trafficControlScript: TrafficControlScript,
    private val redisExecuteAdapter: RedisExecuteAdapter,

    @Value("\${ktc.traffic.control.defaultThreshold}")
    private val defaultThreshold: String
    
) : TrafficControlPort {

    override suspend fun controlTraffic(token: TrafficToken): TrafficWaiting {
        val script = trafficControlScript.getScript()
        val now = Instant.now().epochSecond
        val score = now * 1000 + (now % 1000)

        val keys = TrafficCacheKey.generateKeys(token.zoneId)
        val args = listOf(
            token.token,
            score.toString(),
            now.toString(),
            defaultThreshold
        )

        val result = redisExecuteAdapter.execute(script, keys, args)
        return TrafficWaiting(
            number = result[0] as Long,
            estimatedTime = (result[1] as Long) * 1000,
            totalCount = result[2] as Long
        )
    }

} 