package com.kona.ktc.v1.infrastructure.adapter.redis

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
    private val defaultThreshold: Long
    
) : TrafficControlPort {

    override suspend fun controlTraffic(token: TrafficToken): TrafficWaiting {
        val script = trafficControlScript.getScript()
        val now = Instant.now().epochSecond
        val score = now * 1000 + (now % 1000)

        val baseKey = "ktc:{${token.zoneId}}"
        val keys = listOf(
            "$baseKey:zqueue",
            "$baseKey:tokens",
            "$baseKey:last_refill_ts",
            "$baseKey:last_entry_ts",
            "$baseKey:threshold"
        )

        val args = listOf(
            token.token,
            score.toString(),
            now.toString(),
            defaultThreshold.toString()
        )

        val result = redisExecuteAdapter.execute(script, keys, args)
        return TrafficWaiting(
            number = result[0] as Long,
            estimatedTime = (result[1] as Long) * 1000,
            totalCount = result[2] as Long
        )
    }

} 