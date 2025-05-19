package com.kona.ktca.v1.domain.service

import com.kona.common.infrastructure.cache.redis.RedisExecuteAdapterImpl
import com.kona.common.infrastructure.enumerate.TrafficCacheKey.QUEUE
import com.kona.common.infrastructure.enumerate.TrafficZoneStatus.ACTIVE
import com.kona.common.testsupport.redis.EmbeddedRedis
import com.kona.ktca.v1.infrastructure.adapter.TrafficZoneFindAdapter
import com.kona.ktca.v1.infrastructure.adapter.TrafficZoneWaitingFindAdapter
import com.kona.ktca.v1.infrastructure.repository.FakeTrafficZoneRepository
import com.kona.ktca.v1.infrastructure.repository.entity.TrafficZoneEntity
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.reactor.awaitSingle
import java.time.Instant
import java.time.LocalDateTime

class TrafficZoneMonitorServiceTest : BehaviorSpec({

    val trafficZoneRepository = FakeTrafficZoneRepository()
    val reactiveStringRedisTemplate = EmbeddedRedis.reactiveStringRedisTemplate
    val redisExecuteAdapter = RedisExecuteAdapterImpl(reactiveStringRedisTemplate)

    val trafficZoneFindAdapter = TrafficZoneFindAdapter(trafficZoneRepository)
    val trafficZoneWaitingFindAdapter = TrafficZoneWaitingFindAdapter(redisExecuteAdapter)

    val trafficZoneMonitorService = TrafficZoneMonitorService(trafficZoneFindAdapter, trafficZoneWaitingFindAdapter)

    given("전체 트래픽 제어 Zone 모니터링 요청되어") {

        `when`("현재 트래픽 제어 활성화된 Zone 없는 경우") {
            val result = trafficZoneMonitorService.monitoring()

            then("Zone 현황 조회 0건 정상 확인한다") {
                result shouldNotBe null
                result.size shouldBe 0
            }
        }

        val zoneId1 = "test-zone-1"
        val zoneId2 = "test-zone-2"

        val token1 = "test-token-1"
        val token2 = "test-token-2"

        trafficZoneRepository.save(TrafficZoneEntity(id = zoneId1, alias = zoneId1, threshold = 1, activationTime = LocalDateTime.now(), status = ACTIVE))
        trafficZoneRepository.save(TrafficZoneEntity(id = zoneId2, alias = zoneId2, threshold = 1, activationTime = LocalDateTime.now(), status = ACTIVE))

        reactiveStringRedisTemplate.opsForZSet().add(QUEUE.getKey(zoneId1), token1, Instant.now().toEpochMilli().toDouble()).awaitSingle()
        reactiveStringRedisTemplate.opsForZSet().add(QUEUE.getKey(zoneId2), token1, Instant.now().toEpochMilli().toDouble()).awaitSingle()
        reactiveStringRedisTemplate.opsForZSet().add(QUEUE.getKey(zoneId2), token2, Instant.now().toEpochMilli().toDouble()).awaitSingle()

        `when`("현재 트래픽 제어 활성화된 Zone 2건인 경우") {
            val result = trafficZoneMonitorService.monitoring()

            then("Zone 현황 조회 결과 정상 확인한다") {
                result shouldNotBe null
                result.size shouldBe 2
            }

            then("'test-zone-1' 트래픽 현황 결과 'waitingNumber : 1, entryCount: 0, estimatedClearTime: 60000' 정상 확인한다") {
                val zone1 = result.find { it.zoneId == zoneId1 }?.waiting
                zone1!! shouldNotBe null
                zone1.entryCount shouldBe 0
                zone1.waitingCount shouldBe 1
                zone1.estimatedClearTime shouldBe 60000
            }

            then("'test-zone-2' 트래픽 현황 결과 'waitingNumber : 2, entryCount: 0, estimatedClearTime: 120000' 정상 확인한다") {
                val zone2 = result.find { it.zoneId == zoneId2 }?.waiting
                zone2!! shouldNotBe null
                zone2.entryCount shouldBe 0
                zone2.waitingCount shouldBe 2
                zone2.estimatedClearTime shouldBe 120000
            }
        }


        `when`("특정 zoneId 기준 트래픽 제어 Zone 없는 경우") {
            val result = trafficZoneMonitorService.monitoring("unknown-zone")

            then("0건 반환 처리 정상 확인한다") {
                result shouldNotBe null
                result.size shouldBe 0
            }
        }

        `when`("특정 zoneId 기준 트래픽 제어 Zone 있는 경우") {
            val result = trafficZoneMonitorService.monitoring(zoneId2)

            then("Zone 현황 조회 결과 '1건' 정상 확인한다") {
                result shouldNotBe null
                result.size shouldBe 1
            }

            then("'test-zone-2' 트래픽 현황 결과 'waitingNumber : 2, entryCount: 0, estimatedClearTime: 120000' 정상 확인한다") {
                val zone2 = result.find { it.zoneId == zoneId2 }?.waiting
                zone2!! shouldNotBe null
                zone2.entryCount shouldBe 0
                zone2.waitingCount shouldBe 2
                zone2.estimatedClearTime shouldBe 120000
            }
        }
    }

})