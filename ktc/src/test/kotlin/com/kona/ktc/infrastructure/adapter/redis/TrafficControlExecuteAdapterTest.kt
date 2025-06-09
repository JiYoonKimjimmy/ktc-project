package com.kona.ktc.infrastructure.adapter.redis

import com.kona.common.infrastructure.enumerate.TrafficCacheKey
import com.kona.common.infrastructure.enumerate.TrafficCacheKey.QUEUE_STATUS
import com.kona.common.infrastructure.enumerate.TrafficZoneStatus
import com.kona.common.infrastructure.util.SIX_SECONDS_MILLIS
import com.kona.common.testsupport.redis.EmbeddedRedis
import com.kona.ktc.domain.model.Traffic
import com.kona.ktc.domain.model.TrafficWaiting
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.longs.shouldBeLessThanOrEqual
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

    val getEntryCount: suspend (String) -> Int = { zoneId ->
        reactiveStringRedisTemplate.opsForValue().getAndAwait(TrafficCacheKey.ENTRY_COUNT.getKey(zoneId))?.toInt() ?: 0
    }

    context("트래픽 제어 Zone 정책 '분당 1건 허용'") {
        given("'token-1 ~ token-10' 까지 Zone 진입 요청하여") {
            val zoneId = "test-zone"
            val totalSize = 10
            val threshold = 1
            val sut = TrafficControlExecuteAdapter(reactiveStringRedisTemplate, threshold.toString())

            // 트래픽 대기 요청 정보 생성
            var traffics = (1..totalSize).map { Traffic(zoneId, "test-token-$it") }.toList()
            var nowMillis = Instant.now()

            val controlTraffic: suspend (Traffic, Instant, Long) -> TrafficWaiting = { traffic, now, extra ->
                logging(traffic, sut.controlTraffic(traffic, now.plusMillis(extra)))
            }

            reactiveStringRedisTemplate.opsForValue().setAndAwait(QUEUE_STATUS.getKey(zoneId), TrafficZoneStatus.ACTIVE.name)

            `when`("'token-1' 1건 진입 허용 성공인 경우") {
                val results = traffics.mapIndexed { index, traffic -> controlTraffic(traffic, nowMillis, index.toLong()) }

                then("'token-1' 진입 허용 정상 확인한다") {
                    results.take(threshold).all { it.canEnter } shouldBe true
                }

                then("'token-2 ~ token-10' 진입 대기 상태 정상 확인한다") {
                    val result = results.drop(threshold)
                    result.all { !it.canEnter } shouldBe true
                    result.forEachIndexed { index, waiting ->
                        waiting.number shouldBe index + 1
                        waiting.estimatedTime shouldBeLessThanOrEqual ((index + 1) * 60000L)
                    }
                    result.size shouldBe 9
                }
            }

            nowMillis = nowMillis.plusMillis(SIX_SECONDS_MILLIS)
            traffics = traffics.drop(threshold)

            `when`("6초 후 'token-2 ~ token-10' 까지 Zone 진입 요청하여") {
                val results = traffics.mapIndexed { index, traffic -> controlTraffic(traffic, nowMillis, index.toLong()) }

                then("'token-2' 진입 허용 정상 확인한다") {
                    results.take(threshold).all { it.canEnter } shouldBe true
                }

                then("'token-3 ~ token-10' 진입 대기 상태 정상 확인한다") {
                    val result = results.drop(threshold)
                    result.all { !it.canEnter } shouldBe true
                    result.forEachIndexed { index, waiting ->
                        waiting.number shouldBe index + 1
                        waiting.estimatedTime shouldBeLessThanOrEqual ((index + 1) * 60000L)
                    }
                    result.size shouldBe 8
                }
            }

            nowMillis = nowMillis.plusMillis(SIX_SECONDS_MILLIS * 2)
            traffics = traffics.drop(threshold + 1)

            `when`("18초 후 'token-3' 제외하고, 'token-4 ~ token-10' 까지 Zone 진입 요청하여") {
                val results = traffics.mapIndexed { index, traffic -> controlTraffic(traffic, nowMillis, index.toLong()) }

                then("'token-4' 진입 허용 정상 확인한다") {
                    results.take(threshold).all { it.canEnter } shouldBe true
                }

                then("'token-5 ~ token-10' 진입 대기 상태 정상 확인한다") {
                    val result = results.drop(threshold)
                    result.all { !it.canEnter } shouldBe true
                    result.size shouldBe 6
                }
            }
        }
    }

    context("트래픽 제어 Zone 정책 '분당 10건 허용'") {
        given("트래픽 '1 ~ 20' 까지 3초 단위 Polling 진입 요청하여") {
            val zoneId = "zone-1-to-20"
            val totalSize = 20
            val threshold = 10
            val secondThreshold = threshold / 10
            val sut = TrafficControlExecuteAdapter(reactiveStringRedisTemplate, threshold.toString())

            // 트래픽 대기 요청 정보 생성
            var traffics = (1..totalSize).map { Traffic(zoneId, "test-token-$it") }.toList()
            var nowMillis = Instant.now()

            val controlTraffic: suspend (Int, Traffic, Instant) -> TrafficWaiting = { index, traffic, now ->
                sut.controlTraffic(traffic, now.plusMillis(index.toLong() * 2 + 1))
            }

            reactiveStringRedisTemplate.opsForValue().setAndAwait(QUEUE_STATUS.getKey(zoneId), TrafficZoneStatus.ACTIVE.name)

            var results = emptyList<TrafficWaiting>()
            var min = 0
            var sec = 0
            var startIndex = 0
            val expectedEntryCount = AtomicInteger(0)

            while ((results.isEmpty() || results.all { it.canEnter }.not())) {
                `when`("[${min}:${sec} 경과] 트래픽 '${startIndex + 1} ~ ${startIndex + secondThreshold}' 까지 '${secondThreshold}건' 진입 요청하여") {
                    // 다음 그룹 트레픽 진입 요청 처리
                    results = traffics.mapIndexed { index, traffic ->
                        logging(traffic, controlTraffic(index, traffic, nowMillis))
                    }
                    val entryTokens = results.count { it.canEnter }
                    val entryCount = getEntryCount(zoneId)

                    if (entryTokens > 0) {
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
    }

    context("트래픽 제어 Zone 정책 '분당 100건 허용'") {
        given("트래픽 '1 ~ 200' 까지 3초 단위 Polling 진입 요청하여") {
            val zoneId = "zone-1-to-200"
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
                    val entryTokens = results.count { it.canEnter }
                    val entryCount = getEntryCount(zoneId)

                    if (entryTokens > 0) {
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
    }
    
    context("트래픽 제어 Zone 정책 '분당 1000건 허용'") {
        given("'token-1 ~ token-2000' 트래픽 진입 요청하여") {
            val zoneId = "zone-1-to-2000"
            val totalSize = 2000
            val threshold = 1000
            val sut = TrafficControlExecuteAdapter(reactiveStringRedisTemplate, threshold.toString())

            // 트래픽 대기 요청 정보 생성
            val traffics = (1..totalSize).map { Traffic(zoneId, "test-token-$it") }.toList()
            var nowMillis = Instant.now()

            val controlTraffic: suspend (Int, Traffic, Instant) -> TrafficWaiting = { index, traffic, now ->
                sut.controlTraffic(traffic, now.plusMillis(index.toLong() * 2 + 1))
            }

            reactiveStringRedisTemplate.opsForValue().setAndAwait(QUEUE_STATUS.getKey(zoneId), TrafficZoneStatus.ACTIVE.name)

            var results = traffics.mapIndexed { index, traffic ->
                logging(traffic, controlTraffic(index, traffic, nowMillis))
            }
            var entryCount = getEntryCount(zoneId)

            `when`("'token-1 ~ token-100' 까지 진입 허용 처리되는 경우") {

                then("진입 허용 결과 건수 '100건' 정상 확인한다") {
                    results.count { it.canEnter } shouldBe 100
                }

                then("진입 대기 결과 건수 '1900건' 정상 확인한다") {
                    results.count { !it.canEnter } shouldBe 1900
                }

                then("entryCount '100건' 증가 정상 확인한다") {
                    getEntryCount(zoneId) shouldBe 100
                }
            }

            nowMillis = nowMillis.plusMillis(6000)

            `when`("6초 경과 후 'token-201 ~ token-300' 진입 요청 결과 대기 처리되는 경우") {
                results = traffics.subList(200, 300).mapIndexed { index, traffic ->
                    logging(traffic, controlTraffic(index, traffic, nowMillis))
                }

                then("진입 대기 결과 건수 '100건' 정상 확인한다") {
                    results.count { !it.canEnter } shouldBe 100
                }

                then("entryCount '100건' 정상 확인한다") {
                    getEntryCount(zoneId) shouldBe entryCount
                }
            }

            nowMillis = nowMillis.plusMillis(6500)

            `when`("12초 경과 후 'token-201 ~ token-300' 진입 요청 결과 허용 처리되는 경우") {
                results = traffics.subList(200, 300).mapIndexed { index, traffic ->
                    logging(traffic, controlTraffic(index, traffic, nowMillis))
                }

                then("진입 허용 결과 건수 '100건' 정상 확인한다") {
                    results.count { it.canEnter } shouldBe 100
                }

                then("entryCount '100건' 증가 정상 확인한다") {
                    (getEntryCount(zoneId) - entryCount) shouldBe 100
                }

                entryCount = getEntryCount(zoneId)
            }

            nowMillis = nowMillis.plusMillis(60000)

            `when`("1분12초 경과 후 'token-1301 ~ token-1400' 진입 요청 결과 대기되는 경우") {
                results = traffics.subList(1300, 1400).mapIndexed { index, traffic ->
                    logging(traffic, controlTraffic(index, traffic, nowMillis))
                }

                then("진입 대기 결과 건수 '100건' 정상 확인한다") {
                    results.count { !it.canEnter } shouldBe 100
                }

                then("'entryCount: 200' 정상 확인한다") {
                    getEntryCount(zoneId) shouldBe entryCount
                }
            }

            nowMillis = nowMillis.plusMillis(6500)

            `when`("1분18초 경과 후 'token-1301 ~ token-1400' 진입 요청 결과 허용되는 경우") {
                results = traffics.subList(1300, 1400).mapIndexed { index, traffic ->
                    logging(traffic, controlTraffic(index, traffic, nowMillis))
                }

                then("진입 허용 결과 건수 '100건' 정상 확인한다") {
                    results.count { it.canEnter } shouldBe 100
                }

                then("entryCount '100건' 증가 정상 확인한다") {
                    (getEntryCount(zoneId) - entryCount) shouldBe 100
                }
            }
        }
    }

})