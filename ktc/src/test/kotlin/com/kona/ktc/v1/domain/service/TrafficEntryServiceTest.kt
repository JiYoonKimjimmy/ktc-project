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
import kotlinx.coroutines.delay

class TrafficEntryServiceTest : BehaviorSpec({
    listeners(EmbeddedRedisTestListener(), MockRabbitMQTestListener(V1_SAVE_TRAFFIC_STATUS_EXCHANGE))

    val trafficControlScript = TrafficControlScript().also { it.init() }
    val redisExecuteAdapter = RedisExecuteAdapterImpl(EmbeddedRedis.reactiveStringRedisTemplate)
    val defaultThreshold = "1"
    val trafficControlScriptExecuteAdapter = TrafficControlScriptExecuteAdapter(trafficControlScript, redisExecuteAdapter, defaultThreshold)

    val eventPublisher = FakeApplicationEventPublisher()
    val trafficEntryService = TrafficEntryService(trafficControlScriptExecuteAdapter, eventPublisher)

    val zoneId = "test-zone"

    beforeSpec {
        val trafficWaitService = TrafficWaitService(trafficControlScriptExecuteAdapter, eventPublisher)
        trafficWaitService.wait(TrafficToken(zoneId = zoneId, token = "test-token-1"))
        trafficWaitService.wait(TrafficToken(zoneId = zoneId, token = "test-token-2"))
        trafficWaitService.wait(TrafficToken(zoneId = zoneId, token = "test-token-3"))
    }

    given("트래픽 진입 대기 '2건' 60s 대기 후 진입 요청되어") {
        delay(60000)

        val result1 = trafficEntryService.entry(TrafficToken(zoneId = zoneId, token = "test-token-2"))
        val result2 = trafficEntryService.entry(TrafficToken(zoneId = zoneId, token = "test-token-3"))

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