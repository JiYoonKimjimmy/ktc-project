package com.kona.ktca.v1.presentation.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.kona.common.infrastructure.enumerate.TrafficZoneStatus
import com.kona.common.infrastructure.util.convertPatternOf
import com.kona.ktca.dto.V1SaveZoneRequest
import com.kona.ktca.dto.ZoneStatus
import com.kona.ktca.v1.domain.model.TrafficZone
import com.kona.ktca.v1.domain.port.outbound.TrafficZoneSavePort
import io.kotest.core.spec.style.BehaviorSpec
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.notNullValue
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import java.time.LocalDateTime

@AutoConfigureMockMvc
@SpringBootTest
class V1ZoneManagementControllerTest(
    private val mockMvc: MockMvc,
    private val objectMapper: ObjectMapper,
    private val trafficZoneSavePort: TrafficZoneSavePort
) : BehaviorSpec({

    given("트래픽 Zone 정보 저장 API 요청하여") {
        val url = "/api/v1/zone"

        `when`("신규 정보 등록 요청인 경우") {
            val request = V1SaveZoneRequest(
                zoneAlias = "test-zone-alias",
                threshold = 1,
                activationTime = LocalDateTime.now().convertPatternOf()
            )

            val result = mockMvc
                .post(url) {
                    contentType = MediaType.APPLICATION_JSON
                    content = objectMapper.writeValueAsString(request)
                }
                .andDo { print() }

            then("'201 Created' 응답 정상 확인한다") {
                result
                    .andExpect {
                        status { isCreated() }
                        content {
                            jsonPath("$.zoneId", notNullValue())
                        }
                    }
            }
        }

        val activeTrafficZone = TrafficZone("test-zone-id", "test-zone-alias", 1, LocalDateTime.now(), TrafficZoneStatus.ACTIVE)
        trafficZoneSavePort.save(activeTrafficZone)

        `when`("'threshold : 1000' 정보 변경 요청인 경우") {
            val request = V1SaveZoneRequest(
                zoneId = activeTrafficZone.zoneId,
                threshold = 1000
            )

            val result = mockMvc
                .post(url) {
                    contentType = MediaType.APPLICATION_JSON
                    content = objectMapper.writeValueAsString(request)
                }
                .andDo { print() }

            then("'200 Ok' 응답 정상 확인한다") {
                result
                    .andExpect {
                        status { isOk() }
                        jsonPath("$.zoneId", equalTo(activeTrafficZone.zoneId))
                    }
            }
        }

        `when`("존재하지 않는 'zoneId' 정보 변경 요청인 경우") {
            val request = V1SaveZoneRequest(
                zoneId = "not-found-test-zone-id",
                zoneAlias = "test-zone-alias",
                threshold = 1,
                activationTime = LocalDateTime.now().convertPatternOf()
            )

            val result = mockMvc
                .post(url) {
                    contentType = MediaType.APPLICATION_JSON
                    content = objectMapper.writeValueAsString(request)
                }
                .andDo { print() }

            then("'404 Not Found' 응답 정상 확인한다") {
                result
                    .andExpect {
                        status { isNotFound() }
                        content { jsonPath("$.result.code", equalTo("228_1002_100")) }
                        content { jsonPath("$.result.message", equalTo("Traffic Zone Management Service failed. Traffic zone not found.")) }
                    }
            }
        }

        val deleteTrafficZone = TrafficZone("delete-test-zone-id", "test-zone-alias", 1, LocalDateTime.now(), TrafficZoneStatus.DELETED)
        trafficZoneSavePort.save(deleteTrafficZone)

        `when`("이미 'DELETED' 상태인 정보 'status' 정보 변경 요청인 경우") {
            val request = V1SaveZoneRequest(
                zoneId = deleteTrafficZone.zoneId,
                status = ZoneStatus.ACTIVE
            )

            val result = mockMvc
                .post(url) {
                    contentType = MediaType.APPLICATION_JSON
                    content = objectMapper.writeValueAsString(request)
                }
                .andDo { print() }

            then("'400 Bad Request' 응답 정상 확인한다") {
                result
                    .andExpect {
                        status { isBadRequest() }
                        content { jsonPath("$.result.code", equalTo("228_1002_101")) }
                        content { jsonPath("$.result.message", equalTo("Traffic Zone Management Service failed. Deleted traffic zone status not changed.")) }
                    }
            }
        }
    }

})