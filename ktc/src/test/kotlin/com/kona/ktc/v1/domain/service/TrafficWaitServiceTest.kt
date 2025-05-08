package com.kona.ktc.v1.domain.service

import com.kona.common.infrastructure.cache.redis.RedisExecuteAdapterImpl
import com.kona.common.testsupport.rabbit.MockRabbitMQ.Exchange.V1_SAVE_TRAFFIC_STATUS_EXCHANGE
import com.kona.common.testsupport.rabbit.MockRabbitMQTestListener
import com.kona.common.testsupport.redis.EmbeddedRedis
import com.kona.common.testsupport.redis.EmbeddedRedisTestListener
import com.kona.ktc.v1.domain.model.TrafficToken
import com.kona.ktc.v1.infrastructure.adapter.redis.TrafficControlScript
import com.kona.ktc.v1.infrastructure.adapter.redis.TrafficControlScriptExecuteAdapter
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

class TrafficWaitServiceTest : BehaviorSpec({

    listeners(EmbeddedRedisTestListener(), MockRabbitMQTestListener(V1_SAVE_TRAFFIC_STATUS_EXCHANGE))

    val trafficControlScript = TrafficControlScript().also { it.init() }
    val redisExecuteAdapter = RedisExecuteAdapterImpl(EmbeddedRedis.reactiveStringRedisTemplate)
    val defaultThreshold = "1"
    val trafficControlScriptExecuteAdapter = TrafficControlScriptExecuteAdapter(trafficControlScript, redisExecuteAdapter, defaultThreshold)

    val eventPublisher = FakeApplicationEventPublisher()
    val trafficWaitService = TrafficWaitService(trafficControlScriptExecuteAdapter, eventPublisher)

    given("트래픽 대기 3건 요청되어") {
        val zoneId = "test-zone"
        val result1 = trafficWaitService.wait(TrafficToken(zoneId = zoneId, token = "test-token-1"))
        val result2 = trafficWaitService.wait(TrafficToken(zoneId = zoneId, token = "test-token-2"))
        val result3 = trafficWaitService.wait(TrafficToken(zoneId = zoneId, token = "test-token-3"))

        `when`("첫 번째 요청 - 즉시 입장 가능한 경우") {
            then("트래픽 대기 정보 결과 정상 확인한다") {
                result1.number shouldBe 1
                result1.estimatedTime shouldBe 0
                result1.totalCount shouldBe 1
                result1.canEnter shouldBe true
            }
        }

        `when`("두 번째 요청 - 대기 순번 '1', 예상 대기 시간 '60s' 인 경우") {
            then("트래픽 대기 정보 결과 정상 확인한다") {
                result2.number shouldBe 1
                result2.estimatedTime shouldBe 60000
                result2.totalCount shouldBe 1
                result2.canEnter shouldBe false
            }
        }

        `when`("세 번째 요청 - 대기 순번 '2', 예상 대기 시간 '120s' 인 경우") {
            then("트래픽 대기 정보 결과 정상 확인한다") {
                result3.number shouldBe 2
                result3.estimatedTime shouldBe 120000
                result3.totalCount shouldBe 2
                result3.canEnter shouldBe false
            }
        }
    }

})