package com.kona.ktca.infrastructure.adapter

import com.kona.common.infrastructure.enumerate.TrafficCacheKey
import com.kona.common.testsupport.redis.EmbeddedRedis
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import org.springframework.data.redis.core.addAndAwait
import org.springframework.data.redis.core.setAndAwait
import org.springframework.data.redis.core.sizeAndAwait
import java.time.Instant

class TrafficTokenExpireExecuteAdapterTest : BehaviorSpec({

    val reactiveStringRedisTemplate = EmbeddedRedis.reactiveStringRedisTemplate

    val trafficTokenExpireExecuteAdapter = TrafficTokenExpireExecuteAdapter(reactiveStringRedisTemplate)

    given("트래픽 만료 처리 요청되어") {
        val zoneId = "test-zone"
        val totalSize = 10
        val queueKey = TrafficCacheKey.QUEUE.getKey(zoneId)
        val thresholdKey = TrafficCacheKey.THRESHOLD.getKey(zoneId)
        val tokenLastPollingTimeKey = TrafficCacheKey.TOKEN_LAST_POLLING_TIME.getKey(zoneId)

        var now = Instant.now()

        reactiveStringRedisTemplate.opsForSet().addAndAwait(TrafficCacheKey.ACTIVATION_ZONES.key, zoneId)
        reactiveStringRedisTemplate.opsForValue().setAndAwait(thresholdKey, "1")

        val tokens = (1..totalSize).map {
            val token = "test-token-$it"
            reactiveStringRedisTemplate.opsForZSet().addAndAwait(queueKey, token, now.toEpochMilli().toDouble())
            reactiveStringRedisTemplate.opsForZSet().addAndAwait(tokenLastPollingTimeKey, token, now.toEpochMilli().toDouble())
            token
        }

        now = now.plusMillis(59000)

        `when`("59초 경과 후, 트래픽 마지막 polling 시간 '6초' 경과한 token 없는 경우") {
            trafficTokenExpireExecuteAdapter.expireTraffic(now)

            then("트래픽 대기 Queue 건수 변경 없음 정상 확인한다") {
                reactiveStringRedisTemplate.opsForZSet().sizeAndAwait(queueKey) shouldBe 10
                reactiveStringRedisTemplate.opsForZSet().sizeAndAwait(tokenLastPollingTimeKey) shouldBe 10
            }
        }

        // 제일 앞 순번인 2건 제외하고 모두 polling 요청
        tokens.drop(2).forEach { token ->
            reactiveStringRedisTemplate.opsForZSet().addAndAwait(tokenLastPollingTimeKey, token, now.toEpochMilli().toDouble())
        }

        now = now.plusMillis(1000)

        `when`("60초 경과 후, 트래픽 마지막 polling 시간 '60초' 경과한 token 2건 있는 경우") {
            trafficTokenExpireExecuteAdapter.expireTraffic(now)

            then("트래픽 대기 Queue 건수 '2건' 감소 정상 확인한다") {
                reactiveStringRedisTemplate.opsForZSet().sizeAndAwait(queueKey) shouldBe 8
                reactiveStringRedisTemplate.opsForZSet().sizeAndAwait(tokenLastPollingTimeKey) shouldBe 8
            }
        }
    }

})