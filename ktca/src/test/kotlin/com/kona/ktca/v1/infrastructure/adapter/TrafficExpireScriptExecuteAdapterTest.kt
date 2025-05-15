package com.kona.ktca.v1.infrastructure.adapter

import com.kona.common.infrastructure.cache.redis.RedisExecuteAdapterImpl
import com.kona.common.infrastructure.enumerate.TrafficCacheKey.ACTIVATION_ZONES
import com.kona.common.infrastructure.enumerate.TrafficCacheKey.QUEUE
import com.kona.common.infrastructure.util.toInstantEpochMilli
import com.kona.common.testsupport.redis.EmbeddedRedis
import com.kona.ktca.v1.infrastructure.redis.TrafficExpireRedisScript
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.reactor.awaitSingle
import java.time.Instant

class TrafficExpireScriptExecuteAdapterTest : BehaviorSpec({

    val reactiveStringRedisTemplate = EmbeddedRedis.reactiveStringRedisTemplate

    val trafficExpireRedisScript = TrafficExpireRedisScript()
    val redisScriptExecuteAdapter = RedisExecuteAdapterImpl(reactiveStringRedisTemplate)
    val trafficExpireScriptExecuteAdapter = TrafficExpireScriptExecuteAdapter(trafficExpireRedisScript, redisScriptExecuteAdapter)

    beforeSpec {
        trafficExpireRedisScript.init()
        trafficExpireScriptExecuteAdapter.init()
    }

    given("동일한 Zone 트래픽 3건 대기 중 만료 script 호출하여") {
        val zoneId = "ZONE_1"
        val queueKey = QUEUE.getKey(zoneId)

        val token1 = "token1"
        val token2 = "token2"
        val token3 = "token3"

        val now = Instant.now()
        // 정상 토큰 score : 현재 시간
        val score = now.toInstantEpochMilli()
        // 만료 토큰 score : 현재 시간 - 3분
        val expiredScore = now.minusSeconds(180).toInstantEpochMilli()

        reactiveStringRedisTemplate.opsForSet().add(ACTIVATION_ZONES.key, zoneId).awaitSingle()
        reactiveStringRedisTemplate.opsForZSet().add(queueKey, token1, expiredScore.toDouble()).awaitSingle()
        reactiveStringRedisTemplate.opsForZSet().add(queueKey, token2, score.toDouble()).awaitSingle()
        reactiveStringRedisTemplate.opsForZSet().add(queueKey, token3, score.toDouble()).awaitSingle()

        `when`("현재 시간 기준 대기 시간 3분 지난 만료 Token 1건 존재하는 경우") {
            val result = trafficExpireScriptExecuteAdapter.execute()
            
            then("1건 만료 처리 결과 정상 확인한다") {
                result shouldBe 1
            }

            then("해당 Zone 대기 중인 트래픽 2건 대기 정상 확인한다") {
                val count = reactiveStringRedisTemplate.opsForZSet().size(queueKey).awaitSingle()
                count shouldBe 2
            }
        }
    }
    
})