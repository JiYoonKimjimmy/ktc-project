package com.kona.ktc.infrastructure.adapter.redis

import com.kona.common.infrastructure.enumerate.TrafficCacheKey.QUEUE_STATUS
import com.kona.common.infrastructure.enumerate.TrafficZoneStatus
import com.kona.common.infrastructure.util.ONE_MINUTE_MILLIS
import com.kona.common.infrastructure.util.QUEUE_ACTIVATION_TIME_KEY
import com.kona.common.infrastructure.util.QUEUE_STATUS_KEY
import com.kona.common.testsupport.redis.EmbeddedRedis
import com.kona.ktc.domain.model.Traffic
import com.kona.ktc.domain.model.TrafficWaiting
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.data.forAll
import io.kotest.data.row
import io.kotest.matchers.shouldBe
import org.springframework.data.redis.core.putAllAndAwait
import java.time.Instant
import java.util.concurrent.atomic.AtomicInteger

class TrafficControlExecuteAdapterTest : BehaviorSpec({

    val reactiveStringRedisTemplate = EmbeddedRedis.reactiveStringRedisTemplate

    val logging: suspend (Traffic, TrafficWaiting) -> TrafficWaiting = { traffic, result ->
        val isLogging = true
        val number = result.number
        val estimatedTime = result.estimatedTime
        val totalSeconds = result.estimatedTime / 1000
        val formatEstimatedTime = "${totalSeconds / 60}분 ${totalSeconds % 60}초"
        val totalCount = result.totalCount
        val canEnter = result.canEnter
        if (isLogging) {
            println("token: ${traffic.token}, number: $number, totalCount: $totalCount, canEnter: $canEnter, estimatedTime: ${estimatedTime}ms ($formatEstimatedTime)")
        }
        result
    }

    val controlTraffics: suspend (TrafficControlExecuteAdapter, List<Traffic>, Instant) -> List<TrafficWaiting> = { sut, traffics, now ->
        traffics
            .map { traffic -> logging(traffic, (sut.controlTraffic(traffic, now))) }
            .sortedByDescending{ it.result }
    }

    val generateQueueStatus: suspend (String) -> Unit = { zoneId ->
        val key = QUEUE_STATUS.getKey(zoneId)
        val map = mapOf(
            QUEUE_STATUS_KEY to TrafficZoneStatus.ACTIVE.name,
            QUEUE_ACTIVATION_TIME_KEY to Instant.now().minusMillis(ONE_MINUTE_MILLIS).toEpochMilli().toString()
        )
        reactiveStringRedisTemplate.opsForHash<String, String>().putAllAndAwait(key, map)
    }

    context("트래픽 제어 Zone 요청 건수별 테스트'") {
        forAll(
            row(10, 1),
            row(20, 10),
            row(200, 100),
            row(3000, 1000),
        ) { totalSize, threshold ->
            given("트래픽 '1 ~ $totalSize', 'threshold: $threshold' 진입 3s Polling 요청하여") {
                val zoneId = "zone-1-to-$totalSize"
                val secondThreshold = (threshold / 10).coerceAtLeast(1)
                val sut = TrafficControlExecuteAdapter(reactiveStringRedisTemplate, defaultThreshold = threshold.toString())

                // 트래픽 대기 요청 정보 생성
                var traffics = (1..totalSize).map { Traffic(zoneId, "test-token-$it") }.toList()
                var nowMillis = Instant.now()

                // 트래픽 zone status 정보 생성
                generateQueueStatus(zoneId)

                var results = emptyList<TrafficWaiting>()

                `when`("최초 트래픽 '1 ~ $threshold' 까지 즉시 진입 허용하여") {
                    results = controlTraffics(sut, traffics, nowMillis)

                    then("트래픽 진입 시점 'entryCount: ${threshold}건' 정상 확인한다") {
                        results.take(threshold).all { it.canEnter } shouldBe true
                        results.take(threshold).count { it.canEnter } shouldBe threshold
                    }
                    traffics = traffics.subList(threshold, traffics.size)
                }

                var min = 0
                var sec = 3
                var startIndex = threshold
                val expectedEntryCount = AtomicInteger(startIndex)

                nowMillis = nowMillis.plusMillis(3000)

                println("After $sec seconds...")

                while (results.all { it.canEnter }.not()) {
                    `when`("[${min}:${sec} 경과] 트래픽 '${startIndex + 1} ~ $totalSize' 까지 진입 Polling 요청 결과") {
                        // 다음 그룹 트레픽 진입 요청 처리
                        results = controlTraffics(sut, traffics, nowMillis)

                        val entryTokens = results.count { it.canEnter }
                        val entryCount = expectedEntryCount.addAndGet(entryTokens)

                        if (entryTokens > 0) {
                            then("트래픽 진입 시점 'entryCount: ${entryCount}건' 정상 확인한다") {
                                results.take(secondThreshold).all { it.canEnter } shouldBe true
                                entryTokens shouldBe secondThreshold
                            }
                            startIndex += secondThreshold
                            // 트래픽 진입 성공 그룹 제외하여 트래픽 요청 목록 재생성
                            traffics = traffics.subList(secondThreshold, traffics.size)
                        } else {
                            then("트래픽 대기 시점 'entryCount: ${entryCount}건' 정상 확인한다") {
                                results.take(secondThreshold).all { it.canEnter } shouldBe false
                                entryTokens shouldBe 0
                            }
                        }

                        sec += 3
                        println("After 3 seconds...(total $sec seconds)")
                        nowMillis = nowMillis.plusMillis(3000)

                        if (sec % 60 == 0) {
                            min += 1
                            sec = 0
                        }
                    }
                }
            }
        }
    }
    
    context("트래픽 제어 Zone 동시 제어 테스트'") {
        val totalSize = 3000
        val threshold = 1000

        given("'token-1 ~ token-$totalSize' 트래픽 'threshold: $threshold' 진입 요청하여") {
            val zoneId = "test-zone-1-to-$totalSize"
            val sut = TrafficControlExecuteAdapter(reactiveStringRedisTemplate, defaultThreshold = threshold.toString())

            // 트래픽 대기 요청 정보 생성
            val traffics = (1..totalSize).map { Traffic(zoneId, "test-token-$it") }.toList()
            var nowMillis = Instant.now()

            // 트래픽 zone status 정보 생성
            generateQueueStatus(zoneId)

            var results = controlTraffics(sut, traffics, nowMillis)
            val expectedEntryCount = AtomicInteger(0)

            `when`("'token-1 ~ token-$threshold' 까지 즉시 진입 허용 처리되는 경우") {

                then("진입 허용 결과 건수 '${threshold}건' 정상 확인한다") {
                    val entryCount = results.count { it.canEnter }
                    entryCount shouldBe threshold
                    expectedEntryCount.addAndGet(entryCount)
                }

                then("진입 대기 결과 건수 '${totalSize - threshold}건' 정상 확인한다") {
                    results.count { !it.canEnter } shouldBe totalSize - threshold
                }
            }

            nowMillis = nowMillis.plusMillis(6000)
            println("After 6 seconds...")

            val entry12secTraffics = traffics.subList(1100, 1200)
            val entry12secTrafficsStart = entry12secTraffics.first().token
            val entry12secTrafficsEnd = entry12secTraffics.last().token

            `when`("6초 경과 후 '$entry12secTrafficsStart ~ $entry12secTrafficsEnd' 진입 요청 결과 대기 처리되는 경우") {
                results = controlTraffics(sut, entry12secTraffics, nowMillis)

                then("진입 대기 결과 건수 '100건' 정상 확인한다") {
                    results.count { !it.canEnter } shouldBe 100
                }
            }

            nowMillis = nowMillis.plusMillis(6000)
            println("After 6 seconds...")

            `when`("12초 경과 후 '$entry12secTrafficsStart ~ $entry12secTrafficsEnd' 진입 요청 결과 허용 처리되는 경우") {
                results = controlTraffics(sut, entry12secTraffics, nowMillis)

                then("진입 허용 결과 건수 '100건' 정상 확인한다") {
                    val entryCount = results.count { it.canEnter }
                    entryCount shouldBe 100
                    expectedEntryCount.addAndGet(entryCount)
                }
            }

            nowMillis = nowMillis.plusMillis(60000)
            println("After 60 seconds...")

            val entry1min24secTraffics = traffics.subList(2300, 2400)
            val entry1min24secTrafficsStart = entry12secTraffics.first().token
            val entry1min24secTrafficsEnd = entry12secTraffics.last().token

            `when`("1분12초 경과 후 '$entry1min24secTrafficsStart ~ $entry1min24secTrafficsEnd' 진입 요청 결과 대기되는 경우") {
                results = controlTraffics(sut, entry1min24secTraffics, nowMillis)

                then("진입 대기 결과 건수 '100건' 정상 확인한다") {
                    results.count { !it.canEnter } shouldBe 100
                }
            }

            nowMillis = nowMillis.plusMillis(6000)
            println("After 6 seconds...")

            `when`("1분24초 경과 후 '$entry1min24secTrafficsStart ~ $entry1min24secTrafficsEnd' 진입 요청 결과 허용되는 경우") {
                results = controlTraffics(sut, entry1min24secTraffics, nowMillis)

                then("진입 허용 결과 건수 '100건' 정상 확인한다") {
                    val entryCount = results.count { it.canEnter }
                    entryCount shouldBe 100
                    expectedEntryCount.addAndGet(entryCount)
                }
            }
        }
    }

})