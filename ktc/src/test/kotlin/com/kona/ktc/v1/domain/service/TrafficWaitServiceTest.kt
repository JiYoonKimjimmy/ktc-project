package com.kona.ktc.v1.domain.service

import com.kona.ktc.v1.domain.model.TrafficToken
import com.kona.ktc.v1.domain.model.TrafficWaiting
import com.kona.ktc.v1.domain.port.outbound.TrafficControlPort
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk

class TrafficWaitServiceTest : BehaviorSpec({
    val trafficControlPort = mockk<TrafficControlPort>()
    val trafficWaitService = TrafficWaitService(trafficControlPort)

    given("트래픽 진입 요청되어") {
        val zoneId = "test-zone"
        val token = "test-token"
        val trafficToken = TrafficToken(zoneId = zoneId, token = token)

        `when`("대기 순번 '1' & 예상 대기 시간 '60s' 인 경우") {
            val expectedWaiting = TrafficWaiting(
                number = 1L,
                estimatedTime = 60L,
                totalCount = 1L
            )
            
            coEvery { trafficControlPort.controlTraffic(trafficToken) } returns expectedWaiting

            then("트래픽 대기 정보 결과 정상 확인한다") {
                val result = trafficWaitService.wait(trafficToken)

                result shouldBe expectedWaiting
                result.canEnter shouldBe false
            }
        }

        `when`("대기 순번 '2', 예상 대기 시간 '120s' 인 경우") {
            val expectedWaiting = TrafficWaiting(
                number = 2L,
                estimatedTime = 120L,
                totalCount = 2L
            )
            
            coEvery { trafficControlPort.controlTraffic(trafficToken) } returns expectedWaiting

            then("트래픽 대기 정보 결과 정상 확인한다") {
                val result = trafficWaitService.wait(trafficToken)
                
                result shouldBe expectedWaiting
                result.canEnter shouldBe false
            }
        }

        `when`("즉시 진입 가능한 경우") {
            val expectedWaiting = TrafficWaiting(
                number = 1L,
                estimatedTime = 0L,
                totalCount = 0L
            )
            
            coEvery { trafficControlPort.controlTraffic(trafficToken) } returns expectedWaiting

            then("트래픽 대기 정보 결과 정상 확인한다") {
                val result = trafficWaitService.wait(trafficToken)
                
                result shouldBe expectedWaiting
                result.canEnter shouldBe true
            }
        }
    }

})