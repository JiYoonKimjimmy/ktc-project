package com.kona.ktc.v1.infrastructure.adapter.redis

import com.kona.common.infrastructure.cache.redis.RedisExecuteAdapterImpl
import com.kona.common.infrastructure.enumerate.TrafficCacheKey
import com.kona.common.infrastructure.util.ONE_MINUTE_MILLIS
import com.kona.common.testsupport.redis.EmbeddedRedis
import com.kona.ktc.v1.domain.model.Traffic
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

    given("트래픽 제어 요청 되어") {
        val zoneId = "traffic-zone"
        val traffic1 = Traffic(zoneId, "test-token1")
        val traffic2 = Traffic(zoneId, "test-token2")
        val traffic3 = Traffic(zoneId, "test-token3")

        // 트래픽 제어 요청
        var now = Instant.now()
        var result1 = trafficControlScriptExecuteAdapter.controlTraffic(traffic1, now)
        var result2 = trafficControlScriptExecuteAdapter.controlTraffic(traffic2, now.plusMillis(2))
        var result3 = trafficControlScriptExecuteAdapter.controlTraffic(traffic3, now.plusMillis(3))

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

            then("트래픽 진입 Count 캐시 조회 결과 '1' 정상 확인한다") {
                val entryCount = reactiveStringRedisTemplate.opsForValue().getAndAwait(TrafficCacheKey.ENTRY_COUNT.getKey(zoneId))
                entryCount shouldBe "1"
            }
        }

        now = now.plusMillis(ONE_MINUTE_MILLIS)
        result1 = trafficControlScriptExecuteAdapter.controlTraffic(traffic1, now.plusMillis(1))
        result2 = trafficControlScriptExecuteAdapter.controlTraffic(traffic2, now.plusMillis(2))
        result3 = trafficControlScriptExecuteAdapter.controlTraffic(traffic3, now.plusMillis(3))

        `when`("1분 경과 후 1건 진입 / 1건 대기 처리되는 경우") {
            then("트래픽-1(재진입) 'canEnter: false, number: 3' 처리 결과 정상 확인한다") {
                result1.canEnter shouldBe false
                result1.number shouldBe 3
                result1.estimatedTime shouldBe 180000
                result1.totalCount shouldBe 3
            }

            then("트래픽-2 'canEnter: true' 처리 결과 정상 확인한다") {
                result2.canEnter shouldBe true
            }

            then("트래픽-3 'canEnter: false, number: 1' 처리 결과 정상 확인한다") {
                result3.canEnter shouldBe false
                result3.number shouldBe 1
                result3.estimatedTime shouldBe 60000
                result3.totalCount shouldBe 2
            }

            then("트래픽 진입 Count 캐시 조회 결과 '2' 정상 확인한다") {
                val entryCount = reactiveStringRedisTemplate.opsForValue().getAndAwait(TrafficCacheKey.ENTRY_COUNT.getKey(zoneId))
                entryCount shouldBe "2"
            }
        }

        now = now.plusMillis(ONE_MINUTE_MILLIS)
        result3 = trafficControlScriptExecuteAdapter.controlTraffic(traffic3, now.plusMillis(1))
        result1 = trafficControlScriptExecuteAdapter.controlTraffic(traffic1, now.plusMillis(2))

        `when`("2분 경과 후 1건 진입 / 0건 대기 처리되는 경우") {

            then("트래픽-3 'canEnter: true' 처리 결과 정상 확인한다") {
                result3.canEnter shouldBe true
            }

            then("트래픽-1(재진입) 'canEnter: false, number: 1' 처리 결과 정상 확인한다") {
                result1.canEnter shouldBe false
                result1.number shouldBe 1
                result1.estimatedTime shouldBe 60000
                result1.totalCount shouldBe 1
            }

            then("트래픽 진입 Count 캐시 조회 결과 '3' 정상 확인한다") {
                val entryCount = reactiveStringRedisTemplate.opsForValue().getAndAwait(TrafficCacheKey.ENTRY_COUNT.getKey(zoneId))
                entryCount shouldBe "3"
            }
        }

        now = now.plusMillis(ONE_MINUTE_MILLIS)
        val traffic4 = Traffic(zoneId, "test-token4")
        var result4 = trafficControlScriptExecuteAdapter.controlTraffic(traffic4, now.plusMillis(1))

        `when`("1분 경과 후 0건 진입 / 2건 대기 처리되는 경우") {

            then("트래픽-4 'canEnter: false, estimatedTime: 120000, totalCount: 2' 처리 결과 정상 확인한다") {
                result4.canEnter shouldBe false
                result4.number shouldBe 2
                result4.estimatedTime shouldBe 120000
                result4.totalCount shouldBe 2
            }
        }

        now = now.plusMillis(ONE_MINUTE_MILLIS)
        result4 = trafficControlScriptExecuteAdapter.controlTraffic(traffic4, now.plusMillis(1))

        `when`("1분 경과 후 트래픽-4 먼저 대기 요청하는 경우") {

            then("트래픽-4 'canEnter: false, estimatedTime: 120000, totalCount: 2' 처리 결과 정상 확인한다") {
                result4.canEnter shouldBe false
                result4.number shouldBe 2
                result4.estimatedTime shouldBe 120000
                result4.totalCount shouldBe 2
            }
        }

        now = now.plusMillis(ONE_MINUTE_MILLIS)
        result4 = trafficControlScriptExecuteAdapter.controlTraffic(traffic4, now.plusMillis(1))

        `when`("1분 경과 후 트래픽-4 먼저 진입 요청하는 경우") {

            then("트래픽-4 'canEnter: true' 처리 결과 정상 확인한다") {
                result4.canEnter shouldBe true
            }

            then("트래픽 진입 Count 캐시 조회 결과 '4' 정상 확인한다") {
                val entryCount = reactiveStringRedisTemplate.opsForValue().getAndAwait(TrafficCacheKey.ENTRY_COUNT.getKey(zoneId))
                entryCount shouldBe "4"
            }
        }
    }

})