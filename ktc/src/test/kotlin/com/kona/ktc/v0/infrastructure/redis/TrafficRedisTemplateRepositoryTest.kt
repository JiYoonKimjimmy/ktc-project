package com.kona.ktc.v0.infrastructure.redis

import com.kona.ktc.testsupport.RedisTestListener
import com.kona.ktc.v0.domain.model.TrafficToken
import com.kona.ktc.v0.domain.repository.TrafficRepository
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.redis.core.StringRedisTemplate

@SpringBootTest
class TrafficRedisTemplateRepositoryTest(
    private val trafficRedisTemplateRepository: TrafficRepository,
    private val stringRedisTemplate: StringRedisTemplate
) : BehaviorSpec({

    listeners(RedisTestListener(stringRedisTemplate))

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
                val result = trafficRedisTemplateRepository.controlTraffic(token1)

                result.number shouldBe 1L
                result.estimatedTime shouldBe 0L
                result.totalCount shouldBe 0L
            }

            then("두 번째 트래픽 대기 순번 '1' 결과 정상 확인한다") {
                val result = trafficRedisTemplateRepository.controlTraffic(token2)

                result.number shouldBe 1L
                result.estimatedTime shouldBe 60L
                result.totalCount shouldBe 1L
            }

            then("세 번째 트래픽 대기 순번 '2' 결과 정상 확인한다") {
                val result = trafficRedisTemplateRepository.controlTraffic(token3)

                result.number shouldBe 2L
                result.estimatedTime shouldBe 120L
                result.totalCount shouldBe 2L
            }
        }

        `when`("다른 zoneId 기준 각각 요청되는 경우") {
            val token1 = TrafficToken(token = "test-token-1", zoneId = "zone-1")
            val token2 = TrafficToken(token = "test-token-2", zoneId = "zone-2")

            then("각 zoneId 별로 트래픽 별도 관리하여 모두 즉시 진입 가능 결과 정상 확인한다") {
                val result1 = trafficRedisTemplateRepository.controlTraffic(token1)
                result1.number shouldBe 1L
                result1.estimatedTime shouldBe 0L
                result1.totalCount shouldBe 0L

                val result2 = trafficRedisTemplateRepository.controlTraffic(token2)
                result2.number shouldBe 1L
                result2.estimatedTime shouldBe 0L
                result2.totalCount shouldBe 0L
            }
        }
    }

}) 