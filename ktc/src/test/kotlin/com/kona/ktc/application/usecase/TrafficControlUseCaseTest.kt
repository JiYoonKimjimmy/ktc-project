package com.kona.ktc.application.usecase

import com.kona.common.infrastructure.cache.redis.RedisExecuteAdapterImpl
import com.kona.common.infrastructure.util.ONE_MINUTE_MILLIS
import com.kona.common.testsupport.redis.EmbeddedRedis
import com.kona.ktc.domain.model.Traffic
import com.kona.ktc.testsupport.FakeApplicationEventPublisher
import com.kona.ktc.infrastructure.adapter.redis.TrafficControlScript
import com.kona.ktc.infrastructure.adapter.redis.TrafficControlScriptExecuteAdapter
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import java.time.Instant

class TrafficControlUseCaseTest : BehaviorSpec({
    val trafficControlScript = TrafficControlScript().also { it.init() }
    val redisExecuteAdapter = RedisExecuteAdapterImpl(EmbeddedRedis.reactiveStringRedisTemplate)
    val defaultThreshold = "1"
    val trafficControlScriptExecuteAdapter = TrafficControlScriptExecuteAdapter(trafficControlScript, redisExecuteAdapter, defaultThreshold)
    val eventPublisher = FakeApplicationEventPublisher()
    val trafficControlUseCase = TrafficControlUseCase(trafficControlScriptExecuteAdapter, eventPublisher)

    given("트래픽 대기 3건 요청되어") {
        val zoneId = "wait-test-zone"
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
                result2.estimatedTime shouldBe 60000
                result2.totalCount shouldBe 1
            }
        }

        `when`("세 번째 요청 - 대기 순번 '2', 예상 대기 시간 '120s' 인 경우") {
            then("트래픽 대기 정보 결과 정상 확인한다") {
                result3.canEnter shouldBe false
                result3.number shouldBe 2
                result3.estimatedTime shouldBe 120000
                result3.totalCount shouldBe 2
            }
        }
    }

    given("트래픽 진입 대기 '2건' 60s 대기 후 진입 요청되어") {
        val zoneId = "entry-test-zone"
        val now = Instant.now()

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
                result2.estimatedTime shouldBe 60000
            }
        }
    }

})