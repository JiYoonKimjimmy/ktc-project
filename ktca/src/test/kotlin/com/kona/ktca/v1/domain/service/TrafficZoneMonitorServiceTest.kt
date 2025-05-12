package com.kona.ktca.v1.domain.service

import com.kona.common.infrastructure.cache.redis.RedisExecuteAdapterImpl
import com.kona.common.infrastructure.enumerate.TrafficCacheKey.TRAFFIC_THRESHOLD
import com.kona.common.infrastructure.enumerate.TrafficCacheKey.TRAFFIC_ZQUEUE
import com.kona.common.testsupport.redis.EmbeddedRedis
import com.kona.common.testsupport.redis.EmbeddedRedisTestListener
import com.kona.ktca.v1.infrastructure.adapter.TrafficZoneFindAdapter
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.reactor.awaitSingle
import java.time.Instant

class TrafficZoneMonitorServiceTest : BehaviorSpec({

    listeners(EmbeddedRedisTestListener())

    val reactiveStringRedisTemplate = EmbeddedRedis.reactiveStringRedisTemplate
    val redisExecuteAdapter = RedisExecuteAdapterImpl(reactiveStringRedisTemplate)
    val trafficZoneFindAdapter = TrafficZoneFindAdapter(redisExecuteAdapter)
    val trafficZoneMonitorService = TrafficZoneMonitorService(trafficZoneFindAdapter)

    given("전체 트래픽 제어 Zone 모니터링 요청되어") {

        `when`("현재 트래픽 제어 활성화된 Zone 없는 경우") {
            val result = trafficZoneMonitorService.monitoring()

            then("Zone 현황 조회 0건 정상 확인한다") {
                result shouldNotBe null
                result.zones.size shouldBe 0
            }
        }

        val zoneId1 = "test-zone-1"
        val zoneId2 = "test-zone-2"

        val token1 = "test-token-1"
        val token2 = "test-token-2"

        reactiveStringRedisTemplate.opsForZSet().add(TRAFFIC_ZQUEUE.getKey(zoneId1), token1, Instant.now().toEpochMilli().toDouble()).awaitSingle()
        reactiveStringRedisTemplate.opsForZSet().add(TRAFFIC_ZQUEUE.getKey(zoneId2), token1, Instant.now().toEpochMilli().toDouble()).awaitSingle()
        reactiveStringRedisTemplate.opsForZSet().add(TRAFFIC_ZQUEUE.getKey(zoneId2), token2, Instant.now().toEpochMilli().toDouble()).awaitSingle()

        reactiveStringRedisTemplate.opsForValue().set(TRAFFIC_THRESHOLD.getKey(zoneId1), "1").awaitSingle()
        reactiveStringRedisTemplate.opsForValue().set(TRAFFIC_THRESHOLD.getKey(zoneId2), "1").awaitSingle()

        `when`("현재 트래픽 제어 활성화된 Zone 2건인 경우") {
            val result = trafficZoneMonitorService.monitoring()

            then("Zone 현황 조회 결과 정상 확인한다") {
                result shouldNotBe null
                result.zones.size shouldBe 2
            }

            then("'test-zone-1' 트래픽 현황 결과 'waitingNumber : 1, entryCount: 0, estimatedClearTime: 60000' 정상 확인한다") {
                val zone1 = result.zones.find { it.zoneId == zoneId1 }?.waiting
                zone1!! shouldNotBe null
                zone1.waitingCount shouldBe 1
                zone1.entryCount shouldBe 0
                zone1.estimatedClearTime shouldBe 60000
            }

            then("'test-zone-2' 트래픽 현황 결과 'waitingNumber : 2, entryCount: 0, estimatedClearTime: 120000' 정상 확인한다") {
                val zone2 = result.zones.find { it.zoneId == zoneId2 }?.waiting
                zone2!! shouldNotBe null
                zone2.waitingCount shouldBe 2
                zone2.entryCount shouldBe 0
                zone2.estimatedClearTime shouldBe 120000
            }
        }


        `when`("특정 zoneId 기준 트래픽 제어 Zone 없는 경우") {
            val result = trafficZoneMonitorService.monitoring("unknown-zone")

            then("0건 반환 처리 정상 확인한다") {
                result shouldNotBe null
                result.zones.size shouldBe 0
            }
        }

        `when`("특정 zoneId 기준 트래픽 제어 Zone 있는 경우") {
            val result = trafficZoneMonitorService.monitoring(zoneId2)

            then("Zone 현황 조회 결과 '1건' 정상 확인한다") {
                result shouldNotBe null
                result.zones.size shouldBe 1
            }

            then("'test-zone-2' 트래픽 현황 결과 'waitingNumber : 2, entryCount: 0, estimatedClearTime: 120000' 정상 확인한다") {
                val zone2 = result.zones.find { it.zoneId == zoneId2 }?.waiting
                zone2!! shouldNotBe null
                zone2.waitingCount shouldBe 2
                zone2.entryCount shouldBe 0
                zone2.estimatedClearTime shouldBe 120000
            }
        }
    }

})