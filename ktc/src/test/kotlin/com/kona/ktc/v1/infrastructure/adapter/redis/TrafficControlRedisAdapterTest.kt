package com.kona.ktc.v1.infrastructure.adapter.redis

import com.kona.ktc.testsupport.redis.EmbeddedRedis
import com.kona.ktc.testsupport.redis.EmbeddedRedisTestListener
import com.kona.ktc.v1.domain.model.TrafficToken
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

class TrafficControlRedisAdapterTest : BehaviorSpec({

    listeners(EmbeddedRedisTestListener())

    val trafficControlRedisScript = TrafficControlRedisScript().also { it.init() }
    val stringRedisTemplate = EmbeddedRedis.stringRedisTemplate
    val trafficControlPort = TrafficControlRedisAdapter(trafficControlRedisScript, stringRedisTemplate)

    given("트래픽 대기/진입 확인 요청 되어") {
        val zoneId = "test-zone"
        val threshold = "1"

        `when`("동일 zoneId 기준 $threshold 트래픽 허용인 경우") {
            // `threshold` 정책 변경
            val thresholdKey = "ktc:threshold:$zoneId"
            stringRedisTemplate.opsForValue().set(thresholdKey, threshold)

            val token1 = TrafficToken(token = "test-token-1", zoneId = zoneId)
            val token2 = TrafficToken(token = "test-token-2", zoneId = zoneId)
            val token3 = TrafficToken(token = "test-token-3", zoneId = zoneId)

            then("첫 번째 트래픽 '즉시 진입 가능' 결과 정상 확인한다") {
                val result = trafficControlPort.controlTraffic(token1)
                
                result.number shouldBe 1L
                result.estimatedTime shouldBe 0L
                result.totalCount shouldBe 0L
            }

            then("두 번째 트래픽 대기 순번 '1' 결과 정상 확인한다") {
                val result = trafficControlPort.controlTraffic(token2)
                
                result.number shouldBe 1L
                result.estimatedTime shouldBe 60L
                result.totalCount shouldBe 1L
            }

            then("세 번째 트래픽 대기 순번 '2' 결과 정상 확인한다") {
                val result = trafficControlPort.controlTraffic(token3)

                result.number shouldBe 2L
                result.estimatedTime shouldBe 120L
                result.totalCount shouldBe 2L
            }
        }

        `when`("다른 zoneId 기준 각각 요청되는 경우") {
            val token1 = TrafficToken(token = "test-token-1", zoneId = "zone-1")
            val token2 = TrafficToken(token = "test-token-2", zoneId = "zone-2")

            then("각 zoneId 별로 트래픽 별도 관리하여 모두 즉시 진입 가능 결과 정상 확인한다") {
                val result1 = trafficControlPort.controlTraffic(token1)
                result1.number shouldBe 1L
                result1.estimatedTime shouldBe 0L
                result1.totalCount shouldBe 0L

                val result2 = trafficControlPort.controlTraffic(token2)
                result2.number shouldBe 1L
                result2.estimatedTime shouldBe 0L
                result2.totalCount shouldBe 0L
            }
        }
    }
}) 