package com.kona.ktca.v1.infrastructure.adapter.redis

import com.kona.common.infrastructure.redis.RedisExecuteAdapterImpl
import com.kona.common.infrastructure.util.toInstantEpochMilli
import com.kona.common.testsupport.redis.EmbeddedRedis
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.reactor.awaitSingle
import java.time.LocalDateTime

class TrafficExpireScriptExecuteAdapterTest : BehaviorSpec({

    val reactiveStringRedisTemplate = EmbeddedRedis.reactiveStringRedisTemplate

    val trafficExpireScript = TrafficExpireScript()
    val redisScriptExecuteAdapter = RedisExecuteAdapterImpl(reactiveStringRedisTemplate)
    val trafficExpireScriptExecuteAdapter = TrafficExpireScriptExecuteAdapter(trafficExpireScript, redisScriptExecuteAdapter)

    beforeSpec {
        trafficExpireScript.init()
        trafficExpireScriptExecuteAdapter.init()
    }

    given("동일한 Zone 트래픽 3건 대기 중 만료 script 호출하여") {
        val zoneId = "ZONE_1"
        val zqueueKey = "ktc:{$zoneId}:zqueue"
        val lastEntryKey = "ktc:{$zoneId}:last_entry_ts"

        val token1 = "token1"
        val token2 = "token2"
        val token3 = "token3"

        val now = LocalDateTime.parse("2025-04-29T00:00:00")
        // 2025-04-29T00:00:00
        val expiredScore = now.toInstantEpochMilli().toDouble()
        // 2025-04-29T00:01:00
        val score = now.plusMinutes(2).toInstantEpochMilli().toDouble()
        // 2025-04-29T00:01:00
        val lastEntry = now.plusMinutes(1).toInstantEpochMilli()

        reactiveStringRedisTemplate.opsForZSet().add(zqueueKey, token1, expiredScore).awaitSingle()
        reactiveStringRedisTemplate.opsForZSet().add(zqueueKey, token2, score).awaitSingle()
        reactiveStringRedisTemplate.opsForZSet().add(zqueueKey, token3, score).awaitSingle()
        reactiveStringRedisTemplate.opsForValue().set(lastEntryKey, lastEntry).awaitSingle()

        `when`("현재 시간 기준 대기 시간 2분 지난 만료 Token 1건 존재하는 경우") {
            val result = trafficExpireScriptExecuteAdapter.expireTraffic()
            
            then("1건 만료 처리 결과 정상 확인한다") {
                result shouldBe 1
            }

            then("해당 Zone 대기 중인 트래픽 2건 대기 정상 확인한다") {
                val count = reactiveStringRedisTemplate.opsForZSet().size(zqueueKey).awaitSingle()
                count shouldBe 2
            }
        }
    }
    
})