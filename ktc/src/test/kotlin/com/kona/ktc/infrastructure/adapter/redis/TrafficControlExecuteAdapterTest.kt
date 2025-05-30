package com.kona.ktc.infrastructure.adapter.redis

import com.kona.common.infrastructure.enumerate.TrafficCacheKey
import com.kona.common.infrastructure.enumerate.TrafficCacheKey.QUEUE_STATUS
import com.kona.common.infrastructure.enumerate.TrafficZoneStatus
import com.kona.common.infrastructure.error.ErrorCode
import com.kona.common.infrastructure.error.exception.InternalServiceException
import com.kona.common.testsupport.redis.EmbeddedRedis
import com.kona.ktc.domain.model.Traffic
import com.kona.ktc.domain.model.TrafficWaiting
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import org.springframework.data.redis.core.getAndAwait
import org.springframework.data.redis.core.setAndAwait
import java.time.Instant
import java.util.concurrent.atomic.AtomicInteger

class TrafficControlExecuteAdapterTest : BehaviorSpec({

    val reactiveStringRedisTemplate = EmbeddedRedis.reactiveStringRedisTemplate

    val logging: suspend (Traffic, TrafficWaiting) -> TrafficWaiting = { traffic, result ->
        val number = result.number
        val estimatedTime = result.estimatedTime
        val totalSeconds = result.estimatedTime / 1000
        val formatEstimatedTime = "${totalSeconds / 60}분 ${totalSeconds % 60}초"
        val totalCount = result.totalCount
        val canEnter = result.canEnter
        println("token: ${traffic.token}, number: $number, totalCount: $totalCount, canEnter: $canEnter, estimatedTime: ${estimatedTime}ms ($formatEstimatedTime)")
        result
    }

    given("트래픽 Zone 제어 요청 되어") {
        val zoneId = "blocked-test-zone"
        val threshold = 100
        val trafficControlExecuteAdapter = TrafficControlExecuteAdapter(reactiveStringRedisTemplate, threshold.toString())

        reactiveStringRedisTemplate.opsForValue().setAndAwait(QUEUE_STATUS.getKey(zoneId), TrafficZoneStatus.BLOCKED.name)

        `when`("요청 `zoneId` 기준 상태 `BLOCKED` 인 경우") {
            val exception = shouldThrow<InternalServiceException> { trafficControlExecuteAdapter.controlTraffic(Traffic(zoneId, "test-token")) }

            then("'TRAFFIC_ZONE_STATUS_IS_BLOCKED' 예외 발생 정상 확인한다") {
                exception.errorCode shouldBe ErrorCode.TRAFFIC_ZONE_STATUS_IS_BLOCKED
            }
        }
    }

    given("트래픽 '1 ~ 200' 까지 '1분 단위 100건 / 6초 단위 10건' 진입 가능 처리되는 경우") {
        val zoneId = "test-zone"
        val totalSize = 200
        val threshold = 100
        val secondThreshold = threshold / 10
        val sut = TrafficControlExecuteAdapter(reactiveStringRedisTemplate, threshold.toString())

        // 트래픽 대기 요청 정보 생성
        var traffics = (1..totalSize).map { Traffic(zoneId, "test-token-$it") }.toList()
        var nowMillis = Instant.now()

        val controlTraffic: suspend (Int, Traffic, Instant) -> TrafficWaiting = { index, traffic, now ->
            sut.controlTraffic(traffic, now.plusMillis(index.toLong() * 2 + 1))
        }

        val getEntryCount: suspend () -> Int = {
            reactiveStringRedisTemplate.opsForValue().getAndAwait(TrafficCacheKey.ENTRY_COUNT.getKey(zoneId))?.toInt() ?: 0
        }

        reactiveStringRedisTemplate.opsForValue().setAndAwait(QUEUE_STATUS.getKey(zoneId), TrafficZoneStatus.ACTIVE.name)

        var results = emptyList<TrafficWaiting>()
        var min = 0
        var sec = 0
        var startIndex = 0
        val expectedEntryCount = AtomicInteger(0)

        while (results.isEmpty() || results.all { it.canEnter }.not()) {
            `when`("[${min}:${sec} 경과] 트래픽 '${startIndex + 1} ~ ${startIndex + secondThreshold}' 까지 '${secondThreshold}건' 진입 요청하여") {
                // 다음 그룹 트레픽 진입 요청 처리
                results = traffics.mapIndexed { index, traffic ->
                    logging(traffic, controlTraffic(index, traffic, nowMillis))
                }
                val entryCount = getEntryCount()

                println("waitingCount: ${results.count { it.canEnter.not() }}, entryCount: $entryCount")

                if (sec % 6 == 0) {
                    then("트래픽 진입 시점 'entryCount: ${entryCount}건' 정상 확인한다") {
                        results.take(secondThreshold).all { it.canEnter } shouldBe true
                        entryCount shouldBe expectedEntryCount.addAndGet(secondThreshold)
                    }
                    startIndex += secondThreshold
                    // 트래픽 진입 성공 그룹 제외하여 트래픽 요청 목록 재생성
                    traffics = traffics.subList(secondThreshold, traffics.size)
                } else {
                    then("트래픽 대기 시점 'entryCount: ${entryCount}건' 정상 확인한다") {
                        results.take(secondThreshold).all { it.canEnter } shouldBe false
                        entryCount shouldBe expectedEntryCount.get()
                    }
                }

                nowMillis = if (entryCount % threshold == 0) {
                    // entryCount 가 threshold 도달 후, nowMillis + 1분 증가 처리
                    min += 1
                    sec = 0
                    println("After $min minute...")
                    nowMillis.plusMillis(60000)
                } else {
                    // entryCount 가 threshold 도달 전, 6초 증가 처리
                    sec += 3
                    println("After $sec seconds...")
                    nowMillis.plusMillis(3000)
                }
            }
        }
    }

    given("트래픽 '1 ~ 2000' 까지 '1분 단위 500건 / 6초 단위 50건' 진입 가능 처리되는 경우") {
        val zoneId = "test-zone"
        val totalSize = 2000
        val threshold = 100
        val secondThreshold = threshold / 10
        val sut = TrafficControlExecuteAdapter(reactiveStringRedisTemplate, threshold.toString())

        // 트래픽 대기 요청 정보 생성
        var traffics = (1..totalSize).map { Traffic(zoneId, "test-token-$it") }.toList()
        var nowMillis = Instant.now()

        val controlTraffic: suspend (Int, Traffic, Instant) -> TrafficWaiting = { index, traffic, now ->
            sut.controlTraffic(traffic, now.plusMillis(index.toLong() * 2 + 1))
        }

        val getEntryCount: suspend () -> Int = {
            reactiveStringRedisTemplate.opsForValue().getAndAwait(TrafficCacheKey.ENTRY_COUNT.getKey(zoneId))?.toInt() ?: 0
        }

        reactiveStringRedisTemplate.opsForValue().setAndAwait(QUEUE_STATUS.getKey(zoneId), TrafficZoneStatus.ACTIVE.name)

        var results = emptyList<TrafficWaiting>()
        var min = 0
        var sec = 0
        var startIndex = 0
        val expectedEntryCount = AtomicInteger(0)

        while (results.isEmpty() || results.all { it.canEnter }.not()) {
            `when`("[${min}:${sec} 경과] 트래픽 '${startIndex + 1} ~ ${startIndex + secondThreshold}' 까지 '${secondThreshold}건' 진입 요청하여") {
                // 다음 그룹 트레픽 진입 요청 처리
                results = traffics.mapIndexed { index, traffic ->
                    logging(traffic, controlTraffic(index, traffic, nowMillis))
                }
                val entryCount = getEntryCount()

                println("waitingCount: ${results.count { it.canEnter.not() }}, entryCount: $entryCount")

                if (sec % 6 == 0) {
                    then("트래픽 진입 시점 'entryCount: ${entryCount}건' 정상 확인한다") {
                        results.take(secondThreshold).all { it.canEnter } shouldBe true
                        entryCount shouldBe expectedEntryCount.addAndGet(secondThreshold)
                    }
                    startIndex += secondThreshold
                    // 트래픽 진입 성공 그룹 제외하여 트래픽 요청 목록 재생성
                    traffics = traffics.subList(secondThreshold, traffics.size)
                } else {
                    then("트래픽 대기 시점 'entryCount: ${entryCount}건' 정상 확인한다") {
                        results.take(secondThreshold).all { it.canEnter } shouldBe false
                        entryCount shouldBe expectedEntryCount.get()
                    }
                }

                nowMillis = if (entryCount % threshold == 0) {
                    // entryCount 가 threshold 도달 후, nowMillis + 1분 증가 처리
                    min += 1
                    sec = 0
                    println("After $min minute...")
                    nowMillis.plusMillis(60000)
                } else {
                    // entryCount 가 threshold 도달 전, 6초 증가 처리
                    sec += 3
                    println("After $sec seconds...")
                    nowMillis.plusMillis(3000)
                }
            }
        }
    }

})