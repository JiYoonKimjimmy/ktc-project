package com.kona.ktc.v1.domain.model

import com.kona.common.enum.ClientAgent
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class TrafficTokenTest : BehaviorSpec({
    given("TrafficToken 생성 시") {
        val zoneId = "test-zone"
        val token = "test-token"

        `when`("필수 필드만으로 생성하면") {
            val trafficToken = TrafficToken(
                zoneId = zoneId,
                token = token
            )

            then("필수 필드가 정상적으로 설정된다") {
                trafficToken.zoneId shouldBe zoneId
                trafficToken.token shouldBe token
            }

            then("선택적 필드는 null로 설정된다") {
                trafficToken.clientIp shouldBe null
                trafficToken.clientAgent shouldBe null
            }
        }

        `when`("모든 필드를 포함하여 생성하면") {
            val clientIp = "127.0.0.1"
            val clientAgent = ClientAgent.WEB

            val trafficToken = TrafficToken(
                zoneId = zoneId,
                token = token,
                clientIp = clientIp,
                clientAgent = clientAgent
            )

            then("모든 필드가 정상적으로 설정된다") {
                trafficToken.zoneId shouldBe zoneId
                trafficToken.token shouldBe token
                trafficToken.clientIp shouldBe clientIp
                trafficToken.clientAgent shouldBe clientAgent
            }
        }

        `when`("다른 ClientAgent로 생성하면") {
            val trafficToken1 = TrafficToken(
                zoneId = zoneId,
                token = token,
                clientAgent = ClientAgent.WEB
            )
            val trafficToken2 = TrafficToken(
                zoneId = zoneId,
                token = token,
                clientAgent = ClientAgent.ANDROID
            )

            then("동등성 비교가 실패한다") {
                trafficToken1 shouldNotBe trafficToken2
            }
        }

        `when`("동일한 필드값으로 생성된 두 객체를 비교하면") {
            val trafficToken1 = TrafficToken(
                zoneId = zoneId,
                token = token
            )
            val trafficToken2 = TrafficToken(
                zoneId = zoneId,
                token = token
            )

            then("동등성 비교가 성공한다") {
                trafficToken1 shouldBe trafficToken2
            }

            then("해시코드가 동일하다") {
                trafficToken1.hashCode() shouldBe trafficToken2.hashCode()
            }
        }

        `when`("다른 필드값으로 생성된 두 객체를 비교하면") {
            val trafficToken1 = TrafficToken(
                zoneId = zoneId,
                token = token
            )
            val trafficToken2 = TrafficToken(
                zoneId = "different-zone",
                token = "different-token"
            )

            then("동등성 비교가 실패한다") {
                trafficToken1 shouldNotBe trafficToken2
            }

            then("해시코드가 다르다") {
                trafficToken1.hashCode() shouldNotBe trafficToken2.hashCode()
            }
        }
    }
}) 