package com.kona.ktc.application.usecase

import com.kona.common.infrastructure.cache.redis.RedisExecuteAdapterImpl
import com.kona.common.infrastructure.enumerate.TrafficCacheKey.QUEUE_STATUS
import com.kona.common.infrastructure.enumerate.TrafficZoneStatus
import com.kona.common.infrastructure.error.ErrorCode
import com.kona.common.infrastructure.error.exception.InternalServiceException
import com.kona.common.infrastructure.error.exception.ServiceUnavailableException
import com.kona.common.infrastructure.util.ONE_MINUTE_MILLIS
import com.kona.common.infrastructure.util.QUEUE_ACTIVATION_TIME_KEY
import com.kona.common.infrastructure.util.QUEUE_STATUS_KEY
import com.kona.common.testsupport.redis.EmbeddedRedis
import com.kona.ktc.domain.model.Traffic
import com.kona.ktc.infrastructure.adapter.redis.TrafficControlScriptExecuteAdapter
import com.kona.ktc.infrastructure.config.KtcApplicationConfig
import com.kona.ktc.testsupport.FakeApplicationEventPublisher
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.longs.shouldBeLessThan
import io.kotest.matchers.longs.shouldBeLessThanOrEqual
import io.kotest.matchers.shouldBe
import org.springframework.data.redis.core.putAllAndAwait
import java.time.Instant

class TrafficControlUseCaseTest : BehaviorSpec({
    val trafficControlScript = KtcApplicationConfig().trafficControlScript()
    val reactiveStringRedisTemplate = EmbeddedRedis.reactiveStringRedisTemplate
    val redisExecuteAdapter = RedisExecuteAdapterImpl(reactiveStringRedisTemplate)
    val eventPublisher = FakeApplicationEventPublisher()

    val defaultThreshold = "1"
    val trafficControlScriptExecuteAdapter = TrafficControlScriptExecuteAdapter(trafficControlScript, redisExecuteAdapter, defaultThreshold)
    val trafficControlUseCase = TrafficControlUseCase(trafficControlScriptExecuteAdapter, eventPublisher)

    val generateQueueStatus: suspend (String, TrafficZoneStatus, Instant) -> Unit = { zoneId, status, activationTime ->
        val key = QUEUE_STATUS.getKey(zoneId)
        val map = mapOf(
            QUEUE_STATUS_KEY to status.name,
            QUEUE_ACTIVATION_TIME_KEY to activationTime.toEpochMilli().toString()
        )
        reactiveStringRedisTemplate.opsForHash<String, String>().putAllAndAwait(key, map)
    }

    given("트래픽 대기 요청되어") {
        val token = "test-token-1"
        val notExistsZoneId = "not-exists-zone"

        `when`("요청 `zoneId` 기준 트래픽 Zone 정보 없는 경우") {
            val result = shouldThrow<InternalServiceException> { trafficControlUseCase.controlTraffic(Traffic(zoneId = notExistsZoneId, token = token)) }

            then("'TRAFFIC_ZONE_NOT_FOUND'(트래픽 차단) 예외 발생 정상 확인한다") {
                result.errorCode shouldBe ErrorCode.TRAFFIC_ZONE_NOT_FOUND
            }
        }

        val blockedZoneId = "blocked-zone"
        generateQueueStatus(blockedZoneId, TrafficZoneStatus.BLOCKED, Instant.now())

        `when`("요청 `zoneId` 기준 트래픽 Zone 'status: BLOCKED' 인 경우") {
            val result = shouldThrow<InternalServiceException> { trafficControlUseCase.controlTraffic(Traffic(zoneId = blockedZoneId, token = token)) }

            then("'TRAFFIC_ZONE_STATUS_IS_BLOCKED'(트래픽 차단) 예외 발생 정상 확인한다") {
                result.errorCode shouldBe ErrorCode.TRAFFIC_ZONE_STATUS_IS_BLOCKED
            }
        }

        val faulty503ZoneId = "faulty503-zone"
        generateQueueStatus(faulty503ZoneId, TrafficZoneStatus.FAULTY_503, Instant.now())

        `when`("요청 `zoneId` 기준 트래픽 Zone 'status: FAULTY_503' 인 경우") {
            val result = shouldThrow<ServiceUnavailableException> { trafficControlUseCase.controlTraffic(Traffic(zoneId = faulty503ZoneId, token = token)) }

            then("'FAULTY_503_ERROR'(장애 상황) 예외 발생 정상 확인한다") {
                result.errorCode shouldBe ErrorCode.FAULTY_503_ERROR
            }
        }

        val notActivatedZoneId = "not-activated-zone"
        generateQueueStatus(notActivatedZoneId, TrafficZoneStatus.ACTIVE, Instant.now().plusMillis(6000L))

        `when`("요청 `zoneId` 기준 트래픽 'activationTime' 현재 시간보다 이전인 경우") {
            val result = trafficControlUseCase.controlTraffic(Traffic(zoneId = notActivatedZoneId, token = token))

            then("즉시 입장 정상 확인한다") {
                result.canEnter shouldBe true
            }
        }
    }

    given("트래픽 대기 3건 요청되어") {
        val zoneId = "wait-test-zone"

        generateQueueStatus(zoneId, TrafficZoneStatus.ACTIVE, Instant.now().minusMillis(6000L))

        val result1 = trafficControlUseCase.controlTraffic(Traffic(zoneId = zoneId, token = "test-token-1"))
        val result2 = trafficControlUseCase.controlTraffic(Traffic(zoneId = zoneId, token = "test-token-2"))
        val result3 = trafficControlUseCase.controlTraffic(Traffic(zoneId = zoneId, token = "test-token-3"))

        `when`("첫 번째 요청 - 즉시 입장 가능한 경우") {
            then("트래픽 대기 정보 결과 정상 확인한다") {
                result1.canEnter shouldBe true
                result1.number shouldBe 0
                result1.estimatedTime shouldBe 0
                result1.totalCount shouldBe 0
            }
        }

        `when`("두 번째 요청 - 대기 순번 '1', 예상 대기 시간 '60s' 인 경우") {
            then("트래픽 대기 정보 결과 정상 확인한다") {
                result2.canEnter shouldBe false
                result2.number shouldBe 1
                result2.estimatedTime shouldBeLessThanOrEqual 6000
                result2.totalCount shouldBe 1
            }
        }

        `when`("세 번째 요청 - 대기 순번 '2', 예상 대기 시간 '120s' 인 경우") {
            then("트래픽 대기 정보 결과 정상 확인한다") {
                result3.canEnter shouldBe false
                result3.number shouldBe 2
                result3.estimatedTime shouldBeLessThanOrEqual 12000
                result3.totalCount shouldBe 2
            }
        }
    }

    given("트래픽 진입 대기 '2건' 60s 대기 후 진입 요청되어") {
        val zoneId = "entry-test-zone"
        val now = Instant.now()

        generateQueueStatus(zoneId, TrafficZoneStatus.ACTIVE, now.minusMillis(6000L))

        trafficControlUseCase.controlTraffic(Traffic(zoneId = zoneId, token = "test-token-1"), now)
        trafficControlUseCase.controlTraffic(Traffic(zoneId = zoneId, token = "test-token-2"), now)
        trafficControlUseCase.controlTraffic(Traffic(zoneId = zoneId, token = "test-token-3"), now)

        val result1 = trafficControlUseCase.controlTraffic(Traffic(zoneId = zoneId, token = "test-token-2"), now.plusMillis(ONE_MINUTE_MILLIS))
        val result2 = trafficControlUseCase.controlTraffic(Traffic(zoneId = zoneId, token = "test-token-3"), now.plusMillis(ONE_MINUTE_MILLIS))

        `when`("첫 번째 대기 트래픽 '60s' 이후 진입 요청하는 경우") {
            then("요청 결과 '진입 여부 : true' 정상 확인한다") {
                result1.canEnter shouldBe true
                result1.estimatedTime shouldBe 0
            }
        }

        `when`("두 번째 대기 트래픽 '60s' 이후 진입 요청하는 경우") {
            then("요청 결과 '진입 여부 : false' & 'number: 1' & 'eta: 60s' 정상 확인한다") {
                result2.canEnter shouldBe false
                result2.number shouldBe 1
                result2.estimatedTime shouldBeLessThan 6000
            }
        }
    }

})