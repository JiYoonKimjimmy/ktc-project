package com.kona.ktc.domain.model

import com.kona.common.infrastructure.enumerate.ClientAgent
import com.kona.ktc.domain.model.Traffic
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class TrafficTest : BehaviorSpec({
    given("Traffic 생성 시") {
        val zoneId = "test-zone"
        val token = "test-token"

        `when`("필수 필드만으로 생성하면") {
            val traffic = Traffic(
                zoneId = zoneId,
                token = token
            )

            then("필수 필드가 정상적으로 설정된다") {
                traffic.zoneId shouldBe zoneId
                traffic.token shouldBe token
            }

            then("선택적 필드는 null로 설정된다") {
                traffic.clientIp shouldBe null
                traffic.clientAgent shouldBe null
            }
        }

        `when`("모든 필드를 포함하여 생성하면") {
            val clientIp = "127.0.0.1"
            val clientAgent = ClientAgent.WEB

            val traffic = Traffic(
                zoneId = zoneId,
                token = token,
                clientIp = clientIp,
                clientAgent = clientAgent
            )

            then("모든 필드가 정상적으로 설정된다") {
                traffic.zoneId shouldBe zoneId
                traffic.token shouldBe token
                traffic.clientIp shouldBe clientIp
                traffic.clientAgent shouldBe clientAgent
            }
        }

        `when`("다른 ClientAgent로 생성하면") {
            val traffic1 = Traffic(
                zoneId = zoneId,
                token = token,
                clientAgent = ClientAgent.WEB
            )
            val traffic2 = Traffic(
                zoneId = zoneId,
                token = token,
                clientAgent = ClientAgent.ANDROID
            )

            then("동등성 비교가 실패한다") {
                traffic1 shouldNotBe traffic2
            }
        }

        `when`("동일한 필드값으로 생성된 두 객체를 비교하면") {
            val traffic1 = Traffic(
                zoneId = zoneId,
                token = token
            )
            val traffic2 = Traffic(
                zoneId = zoneId,
                token = token
            )

            then("동등성 비교가 성공한다") {
                traffic1 shouldBe traffic2
            }

            then("해시코드가 동일하다") {
                traffic1.hashCode() shouldBe traffic2.hashCode()
            }
        }

        `when`("다른 필드값으로 생성된 두 객체를 비교하면") {
            val traffic1 = Traffic(
                zoneId = zoneId,
                token = token
            )
            val traffic2 = Traffic(
                zoneId = "different-zone",
                token = "different-token"
            )

            then("동등성 비교가 실패한다") {
                traffic1 shouldNotBe traffic2
            }

            then("해시코드가 다르다") {
                traffic1.hashCode() shouldNotBe traffic2.hashCode()
            }
        }
    }
}) 