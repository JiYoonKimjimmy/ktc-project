package com.kona.ktc.v0.domain.model

import com.kona.ktc.v0.domain.model.TrafficToken
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe

class TrafficTokenTest : BehaviorSpec({
    given("TrafficToken 생성 시") {
        val token = "test-token"
        val zoneId = "test-zone"

        `when`("필수 필드만 지정하면") {
            val trafficToken = TrafficToken(
                token = token,
                zoneId = zoneId
            )

            then("필수 필드가 정상적으로 설정되고, 선택 필드는 null이어야 한다") {
                trafficToken.token shouldBe token
                trafficToken.zoneId shouldBe zoneId
                trafficToken.clientIp.shouldBeNull()
                trafficToken.clientAgent.shouldBeNull()
            }
        }

        `when`("모든 필드를 지정하면") {
            val clientIp = "127.0.0.1"
            val clientAgent = "WEB"

            val trafficToken = TrafficToken(
                token = token,
                zoneId = zoneId,
                clientIp = clientIp,
                clientAgent = clientAgent
            )

            then("모든 필드가 정상적으로 설정되어야 한다") {
                trafficToken.token shouldBe token
                trafficToken.zoneId shouldBe zoneId
                trafficToken.clientIp shouldBe clientIp
                trafficToken.clientAgent shouldBe clientAgent
            }
        }
    }
}) 