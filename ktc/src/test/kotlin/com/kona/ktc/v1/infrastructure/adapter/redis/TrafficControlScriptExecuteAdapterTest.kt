package com.kona.ktc.v1.infrastructure.adapter.redis

import com.kona.common.infrastructure.cache.redis.RedisExecuteAdapterImpl
import com.kona.common.infrastructure.enumerate.TrafficControlCacheKey.QUEUE_CURSOR
import com.kona.common.infrastructure.util.ONE_MINUTE_MILLIS
import com.kona.common.testsupport.redis.EmbeddedRedis
import com.kona.ktc.v1.domain.model.TrafficToken
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import org.springframework.data.redis.core.getAndAwait
import java.time.Instant

class TrafficControlScriptExecuteAdapterTest : BehaviorSpec({

    val reactiveStringRedisTemplate = EmbeddedRedis.reactiveStringRedisTemplate

    val trafficControlScript = TrafficControlScript().also { it.init() }
    val redisScriptAdapter = RedisExecuteAdapterImpl(reactiveStringRedisTemplate)
    val defaultThreshold = "1"
    val trafficControlScriptExecuteAdapter = TrafficControlScriptExecuteAdapter(trafficControlScript, redisScriptAdapter, defaultThreshold)

    given("트래픽 제어 3건 요청 되어") {
        val zoneId = "traffic-zone"
        val trafficToken1 = TrafficToken(zoneId, "test-token1")
        val trafficToken2 = TrafficToken(zoneId, "test-token2")
        val trafficToken3 = TrafficToken(zoneId, "test-token3")

        // 트래픽 제어 요청
        var now = Instant.now()
        val result1 = trafficControlScriptExecuteAdapter.controlTraffic(trafficToken1, now)
        var result2 = trafficControlScriptExecuteAdapter.controlTraffic(trafficToken2, now.plusMillis(2))
        var result3 = trafficControlScriptExecuteAdapter.controlTraffic(trafficToken3, now.plusMillis(3))

        `when`("'1건 진입 / 2건 대기' 처리되는 경우") {

            then("트래픽-1 'canEnter: true' 처리 결과 정상 확인한다") {
                result1.canEnter shouldBe true
            }

            then("트래픽-2 'canEnter: false, number: 1' 처리 결과 정상 확인한다") {
                result2.canEnter shouldBe false
                result2.number shouldBe 1
                result2.estimatedTime shouldBe 60000
                result2.totalCount shouldBe 1
            }

            then("트래픽-3 'canEnter: false, number: 2' 처리 결과 정상 확인한다") {
                result3.canEnter shouldBe false
                result3.number shouldBe 2
                result3.estimatedTime shouldBe 120000
                result3.totalCount shouldBe 2
            }

            then("QUEUE_CURSOR 캐시 조회 결과 '0' 정상 확인하다") {
                val cursor = reactiveStringRedisTemplate.opsForValue().getAndAwait(QUEUE_CURSOR.getKey(zoneId))
                cursor shouldBe "0"
            }

        }

        now = now.plusSeconds(ONE_MINUTE_MILLIS)
        result2 = trafficControlScriptExecuteAdapter.controlTraffic(trafficToken2, now.plusMillis(2))
        result3 = trafficControlScriptExecuteAdapter.controlTraffic(trafficToken3, now.plusMillis(3))

        `when`("1분 경과 후 1건 진입 / 1건 대기 처리되는 경우") {

            then("트래픽-2 'canEnter: true' 처리 결과 정상 확인한다") {
                result2.canEnter shouldBe true
            }

            then("트래픽-3 'canEnter: false, number: 1' 처리 결과 정상 확인한다") {
                result3.canEnter shouldBe false
                result3.number shouldBe 1
                result3.estimatedTime shouldBe 60000
                result3.totalCount shouldBe 1
            }

            then("QUEUE_CURSOR 캐시 조회 결과 '1' 정상 확인하다") {
                val cursor = reactiveStringRedisTemplate.opsForValue().getAndAwait(QUEUE_CURSOR.getKey(zoneId))
                cursor shouldBe "1"
            }
        }

        now = now.plusSeconds(ONE_MINUTE_MILLIS)
        result3 = trafficControlScriptExecuteAdapter.controlTraffic(trafficToken3, now)

        `when`("2분 경과 후 1건 진입 / 0건 대기 처리되는 경우") {

            then("트래픽-3 'canEnter: true' 처리 결과 정상 확인한다") {
                result3.canEnter shouldBe true
            }

            then("QUEUE_CURSOR 캐시 조회 결과 '2' 정상 확인하다") {
                val cursor = reactiveStringRedisTemplate.opsForValue().getAndAwait(QUEUE_CURSOR.getKey(zoneId))
                cursor shouldBe "2"
            }
        }
    }

})