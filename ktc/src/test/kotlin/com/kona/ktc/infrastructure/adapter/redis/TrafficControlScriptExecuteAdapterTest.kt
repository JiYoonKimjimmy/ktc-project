package com.kona.ktc.infrastructure.adapter.redis

import com.kona.common.infrastructure.cache.redis.RedisExecuteAdapterImpl
import com.kona.common.infrastructure.enumerate.TrafficCacheKey
import com.kona.common.infrastructure.enumerate.TrafficCacheKey.QUEUE_STATUS
import com.kona.common.infrastructure.enumerate.TrafficZoneStatus
import com.kona.common.infrastructure.error.ErrorCode
import com.kona.common.infrastructure.error.exception.InternalServiceException
import com.kona.common.infrastructure.util.ONE_MINUTE_MILLIS
import com.kona.common.infrastructure.util.ONE_SECONDS_MILLIS
import com.kona.common.testsupport.redis.EmbeddedRedis
import com.kona.ktc.domain.model.Traffic
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import org.springframework.data.redis.core.getAndAwait
import org.springframework.data.redis.core.setAndAwait
import java.time.Instant

class TrafficControlScriptExecuteAdapterTest : BehaviorSpec({

    val reactiveStringRedisTemplate = EmbeddedRedis.reactiveStringRedisTemplate

    val trafficControlScript = TrafficControlScript().also { it.init() }
    val redisScriptAdapter = RedisExecuteAdapterImpl(reactiveStringRedisTemplate)
    var defaultThreshold = "1"
    var trafficControlScriptExecuteAdapter = TrafficControlScriptExecuteAdapter(trafficControlScript, redisScriptAdapter, defaultThreshold)

    given("'threshold: 1' 트래픽 Zone 제어 요청 되어") {
        val zoneId = "traffic-zone"

        `when`("요청 Zone 상태 'BLOCKED' 인 경우") {
            reactiveStringRedisTemplate.opsForValue().setAndAwait(QUEUE_STATUS.getKey(zoneId), TrafficZoneStatus.BLOCKED.name)
            val exception = shouldThrow<InternalServiceException> { trafficControlScriptExecuteAdapter.controlTraffic(Traffic(zoneId, "test-token")) }

            then("'TRAFFIC_ZONE_STATUS_IS_BLOCKED' 예외 발생 정상 확인한다") {
                exception.errorCode shouldBe ErrorCode.TRAFFIC_ZONE_STATUS_IS_BLOCKED
            }
        }

        reactiveStringRedisTemplate.opsForValue().setAndAwait(QUEUE_STATUS.getKey(zoneId), TrafficZoneStatus.ACTIVE.name)

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

    given("'threshold: 100' 트래픽 Zone 제어, 200건 요청 되어") {
        defaultThreshold = "100"
        trafficControlScriptExecuteAdapter = TrafficControlScriptExecuteAdapter(trafficControlScript, redisScriptAdapter, defaultThreshold)

        val zoneId = "test-zone2"
        val traffics = (1..200).map { Traffic(zoneId, "test-token-$it") }.toList()

        // 트래픽 제어 요청
        var now = Instant.now()
        val results1to100 = traffics.take(100).mapIndexed { index, traffic ->
            trafficControlScriptExecuteAdapter.controlTraffic(traffic, now.plusMillis(index.toLong() * 1))
        }
        val results101to200 = traffics.drop(100).mapIndexed { index, traffic ->
            trafficControlScriptExecuteAdapter.controlTraffic(traffic, now.plusMillis(101 + index.toLong() * 1))
        }

        `when`("'100건 진입' 처리되는 경우") {

            then("트래픽-1to100 'canEnter: true' 처리 결과 정상 확인한다") {
                results1to100.forEach { it.canEnter shouldBe true }
            }

            then("트래픽-101, 102, 103 'canEnter: false' 처리 결과 확인한다") {
                results101to200.forEach { it.canEnter shouldBe false }
                val currentSecondBucketSize = reactiveStringRedisTemplate.opsForValue().getAndAwait(TrafficCacheKey.SECOND_BUCKET.getKey(zoneId))
                currentSecondBucketSize shouldBe "2"
            }

            then("트래픽 진입 Count 캐시 조회 결과 '100' 정상 확인한다") {
                val entryCount = reactiveStringRedisTemplate.opsForValue().getAndAwait(TrafficCacheKey.ENTRY_COUNT.getKey(zoneId))
                entryCount shouldBe "100"
            }
        }

        `when`("'1분 뒤 나머지 100건' 처리되는 경우") {
            now = now.plusMillis(ONE_MINUTE_MILLIS)

            var processedCount = 100
            var remainingTraffics = traffics.drop(processedCount)
            var secondsElapsed = 0

            then("초당 트래픽-2건씩 진입하여 100건이 처리될 때까지 결과 확인") {
                while (remainingTraffics.isNotEmpty() && processedCount < 200) {
                    val currentResults = remainingTraffics.take(remainingTraffics.size.coerceAtMost(10)).mapIndexed { index, traffic ->
                        trafficControlScriptExecuteAdapter.controlTraffic(traffic, now.plusMillis(1 + index.toLong() * 1))
                    }

                    currentResults.take(2).forEach { it.canEnter shouldBe true }
                    // TODO 테스트 확인 필요
                    //currentResults.drop(2).forEach { it.canEnter shouldBe false }

                    processedCount += 2
                    val entryCount = reactiveStringRedisTemplate.opsForValue().getAndAwait(TrafficCacheKey.ENTRY_COUNT.getKey(zoneId))
                    entryCount shouldBe processedCount.toString()

                    remainingTraffics = traffics.drop(processedCount)
                    now = now.plusMillis(ONE_SECONDS_MILLIS)
                    secondsElapsed++
                }

                val finalEntryCount = reactiveStringRedisTemplate.opsForValue().getAndAwait(TrafficCacheKey.ENTRY_COUNT.getKey(zoneId))
                finalEntryCount shouldBe "200"  // 100 + 100
                secondsElapsed shouldBe 50
            }
        }
    }

    /**
     *
     */
    given("""
        AS-IS : secondBucket 적용 후 이슈 - secondBucket = 0 이 된 이후 대기열이 발생하지 않으면 1번 유저는 들어가지 못하는 이슈
        TO-BE : secondBucket = 0, 대기열이 없는 상태여도 시간이 지나면 secondBucket 을 리필해야 한다. 
        'threshold: 1' 트래픽 Zone 제어 요청 되어
    """) {
        val zoneId = "test-zone3"
        defaultThreshold = "1"
        trafficControlScriptExecuteAdapter = TrafficControlScriptExecuteAdapter(trafficControlScript, redisScriptAdapter, defaultThreshold)
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
                val currentSecondBucketSize =
                    reactiveStringRedisTemplate.opsForValue().getAndAwait(TrafficCacheKey.SECOND_BUCKET.getKey(zoneId))
                currentSecondBucketSize shouldBe "1"
            }

            then("트래픽-2 'canEnter: false, number: 1' 처리 결과 정상 확인한다") {
                result2.canEnter shouldBe false
                result2.number shouldBe 1
                result2.estimatedTime shouldBe 60000
                result2.totalCount shouldBe 1
                val currentSecondBucketSize =
                    reactiveStringRedisTemplate.opsForValue().getAndAwait(TrafficCacheKey.SECOND_BUCKET.getKey(zoneId))
                currentSecondBucketSize shouldBe "1"
            }

            then("트래픽-3 'canEnter: false, number: 2' 처리 결과 정상 확인한다") {
                result3.canEnter shouldBe false
                result3.number shouldBe 2
                result3.estimatedTime shouldBe 120000
                result3.totalCount shouldBe 2
                val currentSecondBucketSize =
                    reactiveStringRedisTemplate.opsForValue().getAndAwait(TrafficCacheKey.SECOND_BUCKET.getKey(zoneId))
                currentSecondBucketSize shouldBe "1"
            }

            then("트래픽 진입 Count 캐시 조회 결과 '1' 정상 확인한다") {
                val entryCount =
                    reactiveStringRedisTemplate.opsForValue().getAndAwait(TrafficCacheKey.ENTRY_COUNT.getKey(zoneId))
                entryCount shouldBe "1"
            }
        }

        now = now.plusMillis(ONE_SECONDS_MILLIS)
        result2 = trafficControlScriptExecuteAdapter.controlTraffic(traffic2, now.plusMillis(2))
        result3 = trafficControlScriptExecuteAdapter.controlTraffic(traffic3, now.plusMillis(3))

        `when`("1초 후 트래픽-2,3 진입") {
            then("트래픽-2,3 'canEnter: false' 처리 결과 정상 확인한다") {
                result2.canEnter shouldBe false
                result3.canEnter shouldBe false
            }
        }

        now = now.plusMillis(ONE_MINUTE_MILLIS)
        result2 = trafficControlScriptExecuteAdapter.controlTraffic(traffic2, now.plusMillis(2))
        result3 = trafficControlScriptExecuteAdapter.controlTraffic(traffic3, now.plusMillis(3))

        `when`("1분 후 트래픽-2,3 진입") {
            then("트래픽-2 'canEnter: true' 처리 결과 정상 확인한다") {
                result2.canEnter shouldBe true
            }

            then("트래픽-3 'canEnter: false' 처리 결과 정상 확인한다") {
                result3.canEnter shouldBe false
            }

            then("currentSecondBucketSize = 0이 된다.") {
                val currentSecondBucketSize = reactiveStringRedisTemplate.opsForValue().getAndAwait(TrafficCacheKey.SECOND_BUCKET.getKey(zoneId))
                currentSecondBucketSize shouldBe "0"
            }
        }

        now = now.plusMillis(ONE_SECONDS_MILLIS)
        result3 = trafficControlScriptExecuteAdapter.controlTraffic(traffic3, now.plusMillis(3))

        `when`("1초 후 트래픽-3 진입") {
            then("트래픽-3 canEnter: false 처리 결과 정상 확인한다.") {
                result3.canEnter shouldBe false
            }

            then("""
                AS-IS : 하지만 currentSecondBucketSize = 아직도 0이다. 뒤에 대기열이 없는 이상 계속 secondBucket 은 채워지지 않는 이슈
                TO-BE : 대기열이 있을 때만 secondBucket 을 리필하는 것이 아닌, 리필시간이 되면 바로 secondBucket 을 채운다.  
            """) {
                val currentSecondBucketSize = reactiveStringRedisTemplate.opsForValue().getAndAwait(TrafficCacheKey.SECOND_BUCKET.getKey(zoneId))
                currentSecondBucketSize shouldBe "1"
            }
        }

        now = now.plusMillis(ONE_MINUTE_MILLIS)
        result3 = trafficControlScriptExecuteAdapter.controlTraffic(traffic3, now.plusMillis(3))

        `when`("'1분 후 트래픽-3 진입'") {
            then("""
                AS-IS : currentSecondBucketSize = 0 으로 인해 트래픽-3 'canEnter: false'
                TO-BE : currentSecondBucketSize = 1 (대기열이 없으므로 secondBucket은 줄어들지 않는다) 트래픽-3 'canEnter: true 수정'
            """) {
                result3.canEnter shouldBe true
                val currentSecondBucketSize = reactiveStringRedisTemplate.opsForValue().getAndAwait(TrafficCacheKey.SECOND_BUCKET.getKey(zoneId))
                currentSecondBucketSize shouldBe "1"
            }
        }

    }

})