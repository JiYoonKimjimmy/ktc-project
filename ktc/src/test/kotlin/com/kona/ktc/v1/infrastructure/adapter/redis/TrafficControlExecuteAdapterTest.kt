package com.kona.ktc.v1.infrastructure.adapter.redis

import com.kona.common.infrastructure.enumerate.TrafficControlCacheKey.QUEUE_CURSOR
import com.kona.common.infrastructure.util.ONE_MINUTE_MILLIS
import com.kona.common.testsupport.redis.EmbeddedRedis
import com.kona.ktc.v1.domain.model.TrafficToken
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import org.springframework.data.redis.core.getAndAwait
import java.time.Instant

/**
 * [트래픽 제어 프로세스]
 * - 필요한 Cache 정보
 *   - Queue
 *   - Queue-Cursor
 *   - Bucket
 *   - Bucket Refill Time
 *   - Threshold
 *
 * 1. 트래픽 요청 토큰 Queue 저장
 * 2. 현재시간 - bucketRefillTime >= 60000ms(1분) 인 경우, cursor & bucket & bucketRefillTime 업데이트
 *    - cursor = cursor + Threshold - 1
 *    - bucket = Threshold 만큼 token 리필
 *    - bucketRefillTime = 현재 시간 기준 millis 업데이트
 * 3. 트래픽 요청 토큰 rank(순번) 진입 가능 여부 확인
 *    - 진입 가능 조건 : bucketSize > 0 && cursor <= rank < cursor + threshold
 *    3-1. 진입 가능한 경우, `canEnter : true` 반환 처리
 *         - bucket token 차감 처리
 *    3-2. 진입 불가한 경우, 트래픽 대기 정보 반환 처리
 *         - number        : rank - cursor - threshold - bucketSize
 *         - estimatedTime : ceil((rank * bucketSize + 1) / threshold)
 *         - totalCount    : queueSize - cursor - threshold - bucketSize
 */
class TrafficControlExecuteAdapterTest : BehaviorSpec({

    val reactiveStringRedisTemplate = EmbeddedRedis.reactiveStringRedisTemplate
    val defaultThreshold = "1"
    val trafficControlExecuteAdapter = TrafficControlExecuteAdapter(reactiveStringRedisTemplate, defaultThreshold)

    given("트래픽 제어 3건 요청 되어") {
        val zoneId = "test-zone"
        val trafficToken1 = TrafficToken(zoneId, "test-token1")
        val trafficToken2 = TrafficToken(zoneId, "test-token2")
        val trafficToken3 = TrafficToken(zoneId, "test-token3")

        // 트래픽 제어 요청
        var now = Instant.now()
        val result1 = trafficControlExecuteAdapter.controlTraffic(trafficToken1, now)
        var result2 = trafficControlExecuteAdapter.controlTraffic(trafficToken2, now.plusMillis(2))
        var result3 = trafficControlExecuteAdapter.controlTraffic(trafficToken3, now.plusMillis(3))

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
        result2 = trafficControlExecuteAdapter.controlTraffic(trafficToken2, now.plusMillis(2))
        result3 = trafficControlExecuteAdapter.controlTraffic(trafficToken3, now.plusMillis(3))

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
        result3 = trafficControlExecuteAdapter.controlTraffic(trafficToken3, now)

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

    given("'threshold: 1000' 설정 구간별 대기 예상 시간 계산하여") {
        val threshold = 1000L
        val oneMinuteMillis = 60000L
        val queueCursor = 0L
        val bucketSize = 0L

        fun calcEstimatedTime(rank: Long, threshold: Long, queueCursor: Long, bucketSize: Long): Long {
            val number = rank - queueCursor - threshold - bucketSize + 1
            return kotlin.math.ceil(number.toDouble() / threshold).toLong() * oneMinuteMillis
        }

        then("'rank: 999' 대기 예상 시간 '0분' 정상 확인한다") {
            val rank = 999L
            val estimatedTime = calcEstimatedTime(rank, threshold, queueCursor, bucketSize)
            estimatedTime shouldBe 0L
        }

        then("'rank: 1000' 대기 예상 시간 '1분' 정상 확인한다") {
            val rank = 1000L
            val estimatedTime = calcEstimatedTime(rank, threshold, queueCursor, bucketSize)
            estimatedTime shouldBe oneMinuteMillis
        }

        then("'rank: 1001' 대기 예상 시간 '1분' 정상 확인한다") {
            val rank = 1001L
            val estimatedTime = calcEstimatedTime(rank, threshold, queueCursor, bucketSize)
            estimatedTime shouldBe oneMinuteMillis
        }

        then("'rank: 1998' 대기 예상 시간 '1분' 정상 확인한다") {
            val rank = 1998L
            val estimatedTime = calcEstimatedTime(rank, threshold, queueCursor, bucketSize)
            estimatedTime shouldBe oneMinuteMillis
        }

        then("'rank: 2000' 대기 예상 시간 '2분' 정상 확인한다") {
            val rank = 2000L
            val estimatedTime = calcEstimatedTime(rank, threshold, queueCursor, bucketSize)
            estimatedTime shouldBe 2 * oneMinuteMillis
        }

        then("'rank: 2001' 대기 예상 시간 '2분' 정상 확인한다") {
            val rank = 2001L
            val estimatedTime = calcEstimatedTime(rank, threshold, queueCursor, bucketSize)
            estimatedTime shouldBe 2 * oneMinuteMillis
        }

        then("'rank: 2999' 대기 예상 시간 '2분' 정상 확인한다") {
            val rank = 2999L
            val estimatedTime = calcEstimatedTime(rank, threshold, queueCursor, bucketSize)
            estimatedTime shouldBe 2 * oneMinuteMillis
        }

        then("'rank: 3000' 대기 예상 시간 '3분' 정상 확인한다") {
            val rank = 3000L
            val estimatedTime = calcEstimatedTime(rank, threshold, queueCursor, bucketSize)
            estimatedTime shouldBe 3 * oneMinuteMillis
        }
    }

})