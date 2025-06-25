package com.kona.ktca.domain.service

import com.kona.common.infrastructure.cache.redis.RedisExecuteAdapterImpl
import com.kona.common.infrastructure.enumerate.TrafficCacheKey.QUEUE
import com.kona.common.infrastructure.enumerate.TrafficCacheKey.THRESHOLD
import com.kona.common.testsupport.redis.EmbeddedRedis
import com.kona.ktca.domain.model.TrafficZoneFixture
import com.kona.ktca.domain.model.TrafficZoneGroup
import com.kona.ktca.domain.model.TrafficZoneGroupFixture
import com.kona.ktca.infrastructure.adapter.TrafficZoneCachingAdapter
import com.kona.ktca.infrastructure.adapter.TrafficZoneMonitorCachingAdapter
import com.kona.ktca.infrastructure.repository.FakeTrafficZoneGroupRepository
import com.kona.ktca.infrastructure.repository.FakeTrafficZoneMonitorRepository
import com.kona.ktca.infrastructure.repository.FakeTrafficZoneRepository
import com.kona.ktca.testsupport.FakeApplicationEventPublisher
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.ints.shouldBeGreaterThanOrEqual
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.reactor.awaitSingle
import java.time.Instant

class TrafficZoneMonitorCollectServiceTest : BehaviorSpec({

    val reactiveStringRedisTemplate = EmbeddedRedis.reactiveStringRedisTemplate
    val redisExecuteAdapter = RedisExecuteAdapterImpl(reactiveStringRedisTemplate)
    val eventPublisher = FakeApplicationEventPublisher()

    val trafficZoneRepository = FakeTrafficZoneRepository()
    val trafficZoneCachingPort = TrafficZoneCachingAdapter(redisExecuteAdapter)
    val trafficZoneMonitorRepository = FakeTrafficZoneMonitorRepository()
    val trafficZoneMonitorCachingPort = TrafficZoneMonitorCachingAdapter()
    val trafficZoneMonitorCollectService = TrafficZoneMonitorCollectService(
        trafficZoneRepository,
        trafficZoneCachingPort,
        trafficZoneMonitorRepository,
        trafficZoneMonitorCachingPort,
        eventPublisher = eventPublisher
    )

    val trafficZoneGroupRepository = FakeTrafficZoneGroupRepository()

    lateinit var savedGroup: TrafficZoneGroup

    beforeSpec {
        savedGroup = trafficZoneGroupRepository.save(TrafficZoneGroupFixture.giveOne())
    }

    given("전체 트래픽 제어 Zone 모니터링 수집 요청되어") {

        `when`("현재 트래픽 제어 활성화된 Zone 없는 경우") {
            val result = trafficZoneMonitorCollectService.collect()

            then("Zone 현황 수집 결과 '0건' 정상 확인한다") {
                result shouldNotBe null
                result.size shouldBe 0
            }

            then("Zone 모니터링 수집 DB 조회 결과 '0건' 정상 확인한다") {
                val entities = trafficZoneMonitorRepository.findAll()
                entities.size shouldBe 0
            }

            then("Zone 모니터링 Cache 조회 결과 '0건' 정상 확인한다") {
                val caches = trafficZoneMonitorCachingPort.findAllMonitoringLatestResult()
                caches.size shouldBe 0
            }
        }

        val zoneId1 = "test-zone-1"
        val zoneId2 = "test-zone-2"

        val token1 = "test-token-1"
        val token2 = "test-token-2"

        val zone1 = TrafficZoneFixture.giveOne(zoneId = zoneId1, group = savedGroup)
        val zone2 = TrafficZoneFixture.giveOne(zoneId = zoneId2, group = savedGroup)

        trafficZoneRepository.save(zone1)
        trafficZoneRepository.save(zone2)

        reactiveStringRedisTemplate.opsForZSet().add(QUEUE.getKey(zoneId1), token1, Instant.now().toEpochMilli().toDouble()).awaitSingle()
        reactiveStringRedisTemplate.opsForZSet().add(QUEUE.getKey(zoneId2), token1, Instant.now().toEpochMilli().toDouble()).awaitSingle()
        reactiveStringRedisTemplate.opsForZSet().add(QUEUE.getKey(zoneId2), token2, Instant.now().toEpochMilli().toDouble()).awaitSingle()

        reactiveStringRedisTemplate.opsForValue().set(THRESHOLD.getKey(zoneId1), "1").awaitSingle()
        reactiveStringRedisTemplate.opsForValue().set(THRESHOLD.getKey(zoneId2), "1").awaitSingle()

        `when`("현재 트래픽 제어 활성화된 Zone 2건인 경우") {
            val result = trafficZoneMonitorCollectService.collect()

            then("Zone 현황 수집 결과 정상 확인한다") {
                result shouldNotBe null
                result.size shouldBe 2
            }

            then("'test-zone-1' 트래픽 현황 결과 'waitingNumber : 1, entryCount: 0, estimatedClearTime: 60000' 정상 확인한다") {
                val zone1 = result.find { it.zoneId == zoneId1 }
                zone1!! shouldNotBe null
                zone1.entryCount shouldBe 0
                zone1.waitingCount shouldBe 1
                zone1.estimatedClearTime shouldBe 60000
            }

            then("'test-zone-2' 트래픽 현황 결과 'waitingNumber : 2, entryCount: 0, estimatedClearTime: 120000' 정상 확인한다") {
                val zone2 = result.find { it.zoneId == zoneId2 }
                zone2!! shouldNotBe null
                zone2.entryCount shouldBe 0
                zone2.waitingCount shouldBe 2
                zone2.estimatedClearTime shouldBe 120000
            }

            then("Zone 모니터링 수집 DB 조회 결과 '2건' 정상 확인한다") {
                val entities = trafficZoneMonitorRepository.findAll()
                entities.size shouldBe 2
            }

            then("Zone 모니터링 Cache 조회 결과 '2건' 정상 확인한다") {
                val caches = trafficZoneMonitorCachingPort.findAllMonitoringLatestResult()
                caches.size shouldBe 2
            }
        }

        `when`("특정 zoneId 기준 트래픽 제어 Zone 없는 경우") {
            val result = trafficZoneMonitorCollectService.collect("unknown-zone")

            then("0건 반환 처리 정상 확인한다") {
                result shouldNotBe null
                result.size shouldBe 0
            }
        }

        `when`("특정 zoneId 기준 트래픽 제어 Zone 있는 경우") {
            val result = trafficZoneMonitorCollectService.collect(zoneId2)

            then("Zone 현황 수집 결과 '1건' 정상 확인한다") {
                result shouldNotBe null
                result.size shouldBe 1
            }

            then("'test-zone-2' 트래픽 현황 결과 'waitingNumber : 2, entryCount: 0, estimatedClearTime: 120000' 정상 확인한다") {
                val zone2 = result.find { it.zoneId == zoneId2 }
                zone2!! shouldNotBe null
                zone2.entryCount shouldBe 0
                zone2.waitingCount shouldBe 2
                zone2.estimatedClearTime shouldBe 120000
            }

            then("'test-zone-2' Zone 모니터링 수집 DB 조건 조회 결과 '1건 이상' 정상 확인한다") {
                val entities = trafficZoneMonitorRepository.findAll(zoneId2)
                entities.size shouldBeGreaterThanOrEqual 1
            }

            then("'test-zone-2' Zone 모니터링 Cache 조회 결과 '1건' 정상 확인한다") {
                val caches = trafficZoneMonitorCachingPort.findAllMonitoringLatestResult()
                caches.filter { it.zoneId == zoneId2 }.size shouldBe 1
            }
        }
    }

    given("트래픽 제어 Zone 모니터링 수집 '10회' 반복 요청되어") {
        val zoneId1 = "test-zone-1"
        val zone1 = TrafficZoneFixture.giveOne(zoneId = zoneId1, group = savedGroup)
        trafficZoneRepository.save(zone1)

        `when`("최초 '5회' 수집 처리 성공 인 경우") {
            (1..5).sumOf {
                trafficZoneMonitorCollectService.collect().size
            }

            then("Zone 모니터링 결과 DB 조회하여 '5건' 정상 확인한다") {
                val entities = trafficZoneMonitorRepository.findAll()
                entities.size shouldBeGreaterThanOrEqual 5
            }

            then("마지막 Zone 모니터링 결과 Cache 조회하여 '1건' 정상 확인한다") {
                val caches = trafficZoneMonitorCachingPort.findAllMonitoringLatestResult()
                caches.size shouldBeGreaterThanOrEqual 1
            }
        }

        `when`("'5회' 이상 'waitingCount: 0' 반복 5회 초과인 경우") {
            (1..5).sumOf {
                trafficZoneMonitorCollectService.collect().size
            }

            then("Zone 모니터링 결과 DB 추가 저장되지 않고 '5건' 정상 확인한다") {
                val entities = trafficZoneMonitorRepository.findAll()
                entities.size shouldBeGreaterThanOrEqual 5
            }

            then("마지막 Zone 모니터링 결과 Cache 조회하여 '1건' 정상 확인한다") {
                val caches = trafficZoneMonitorCachingPort.findAllMonitoringLatestResult()
                caches.size shouldBeGreaterThanOrEqual 1
            }
        }
    }

})