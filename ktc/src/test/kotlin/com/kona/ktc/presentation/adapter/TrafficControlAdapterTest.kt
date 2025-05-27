package com.kona.ktc.presentation.adapter

import com.kona.common.infrastructure.enumerate.ClientAgent
import com.kona.ktc.application.usecase.TrafficControlStreamUseCase
import com.kona.ktc.application.usecase.TrafficControlUseCase
import com.kona.ktc.domain.model.TrafficWaiting
import com.kona.ktc.presentation.dto.mapper.TrafficMapper
import com.kona.ktc.presentation.dto.request.TrafficWaitRequest
import com.kona.ktc.testsupport.KtcProjectConfig.Companion.mockMvcBuilder
import com.kona.ktc.testsupport.KtcProjectConfig.Companion.objectMapper
import io.kotest.core.spec.style.BehaviorSpec
import io.mockk.coEvery
import io.mockk.mockk
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath

class TrafficControlAdapterTest : BehaviorSpec({
    val trafficMapper = TrafficMapper()
    val trafficControlUseCase = mockk<TrafficControlUseCase>()
    val trafficControlStreamUseCase = mockk<TrafficControlStreamUseCase>()

    val trafficControlAdapter = V1TrafficControlAdapter(trafficControlUseCase, trafficControlStreamUseCase, trafficMapper)

    val mockMvc = mockMvcBuilder(trafficControlAdapter)

    given("트래픽 대기 API 요청되어") {
        val url = "/api/v1/traffic/wait"
        val zoneId = "test-zone"
        val token = "test-token"
        val clientIp = "127.0.0.1"
        val clientAgent = ClientAgent.WEB

        `when`("'요청 token' 정보 포함된 경우") {
            val request = TrafficWaitRequest(zoneId = zoneId, token = token, clientIp = clientIp, clientAgent = clientAgent)

            val expectedWaiting = TrafficWaiting(result = 0, number = 1L, estimatedTime = 60L, totalCount = 1L)
            coEvery { trafficControlUseCase.controlTraffic(any()) } returns expectedWaiting

            then("요청 'token' 정보 기반 트래픽 대기 정보 응답 결과 정상 확인한다") {
                mockMvc
                    .post(url) {
                        contentType = MediaType.APPLICATION_JSON
                        content = objectMapper.writeValueAsString(request)
                    }
                    .andDo { print() }
                    .andExpect {
                        status { is2xxSuccessful() }
                        content {
                            jsonPath("$.canEnter").value(false)
                            jsonPath("$.zoneId").value(zoneId)
                            jsonPath("$.token").value(token)
                            jsonPath("$.waiting.number").value(expectedWaiting.number)
                            jsonPath("$.waiting.estimatedTime").value(expectedWaiting.estimatedTime)
                            jsonPath("$.waiting.totalCount").value(expectedWaiting.totalCount)
                            jsonPath("$.waiting.pollingPeriod").value(expectedWaiting.pollingPeriod)
                            jsonPath("$.result.status").value("SUCCESS")
                        }
                    }
            }
        }

        `when`("요청 'token' 정보 없는 경우") {
            val request = TrafficWaitRequest(zoneId = zoneId, token = null, clientIp = clientIp, clientAgent = clientAgent)

            val expectedWaiting = TrafficWaiting(result = 1, number = 1L, estimatedTime = 0L, totalCount = 0L)
            coEvery {
                trafficControlUseCase.controlTraffic(
                    match { token ->
                        token.zoneId == zoneId &&
                        token.clientIp == clientIp &&
                        token.clientAgent == clientAgent &&
                        token.token.isNotBlank()
                    }
                )
            } returns expectedWaiting

            then("새로운 'token' 생성하여 트래픽 대기 정보 응답 정상 확인한다") {
                mockMvc
                    .post(url) {
                        contentType = MediaType.APPLICATION_JSON
                        content = objectMapper.writeValueAsString(request)
                    }
                    .andDo { print() }
                    .andExpect {
                        status { is2xxSuccessful() }
                        content {
                            jsonPath("$.canEnter").value(true)
                            jsonPath("$.zoneId").value(zoneId)
                            jsonPath("$.token").exists()
                            jsonPath("$.waiting.number").value(expectedWaiting.number)
                            jsonPath("$.waiting.estimatedTime").value(expectedWaiting.estimatedTime)
                            jsonPath("$.waiting.totalCount").value(expectedWaiting.totalCount)
                            jsonPath("$.waiting.pollingPeriod").value(expectedWaiting.pollingPeriod)
                            jsonPath("$.result.status").value("SUCCESS")
                        }
                    }
            }
        }
    }
})