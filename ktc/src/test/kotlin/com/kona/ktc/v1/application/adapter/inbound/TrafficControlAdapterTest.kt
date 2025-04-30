package com.kona.ktc.v1.application.adapter.inbound

import com.kona.common.infrastructure.enumerate.ClientAgent
import com.kona.ktc.testsupport.KtcProjectConfig.Companion.mockMvcBuilder
import com.kona.ktc.testsupport.KtcProjectConfig.Companion.objectMapper
import com.kona.ktc.v1.application.dto.mapper.TrafficControlMapper
import com.kona.ktc.v1.application.dto.request.TrafficWaitRequest
import com.kona.ktc.v1.domain.model.TrafficWaiting
import com.kona.ktc.v1.domain.port.inbound.TrafficEntryPort
import com.kona.ktc.v1.domain.port.inbound.TrafficWaitPort
import io.kotest.core.spec.style.BehaviorSpec
import io.mockk.coEvery
import io.mockk.mockk
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath

class TrafficControlAdapterTest : BehaviorSpec({
    val trafficControlMapper = TrafficControlMapper()
    val trafficWaitPort = mockk<TrafficWaitPort>()
    val trafficEntryPort = mockk<TrafficEntryPort>()
    val trafficControlAdapter = TrafficControlAdapter(trafficControlMapper, trafficWaitPort, trafficEntryPort)

    val mockMvc = mockMvcBuilder(trafficControlAdapter)

    given("트래픽 대기 API 요청되어") {
        val url = "/api/v1/traffic/wait"
        val zoneId = "test-zone"
        val token = "test-token"
        val clientIp = "127.0.0.1"
        val clientAgent = ClientAgent.WEB

        `when`("'요청 token' 정보 포함된 경우") {
            val request = TrafficWaitRequest(zoneId = zoneId, token = token, clientIp = clientIp, clientAgent = clientAgent)

            val expectedWaiting = TrafficWaiting(number = 1L, estimatedTime = 60L, totalCount = 1L)
            coEvery { trafficWaitPort.wait(any()) } returns expectedWaiting

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

            val expectedWaiting = TrafficWaiting(number = 1L, estimatedTime = 0L, totalCount = 0L)
            coEvery {
                trafficWaitPort.wait(
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