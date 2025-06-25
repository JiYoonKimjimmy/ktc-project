package com.kona.ktca.presentation.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.kona.common.infrastructure.enumerate.TrafficZoneStatus
import com.kona.common.infrastructure.util.convertPatternOf
import com.kona.ktca.domain.dto.TrafficZoneDTO
import com.kona.ktca.domain.model.TrafficZoneGroup
import com.kona.ktca.domain.model.TrafficZoneGroupFixture
import com.kona.ktca.domain.port.inbound.TrafficZoneFindPort
import com.kona.ktca.domain.port.inbound.TrafficZoneSavePort
import com.kona.ktca.domain.port.outbound.TrafficZoneGroupRepository
import com.kona.ktca.dto.UpdateZoneStatus
import com.kona.ktca.dto.V1CreateZoneRequest
import com.kona.ktca.dto.V1UpdateZoneRequest
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import org.hamcrest.Matchers.*
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.*
import java.time.LocalDateTime

@AutoConfigureMockMvc
@SpringBootTest
class V1ZoneManagementControllerTest(
    private val mockMvc: MockMvc,
    private val objectMapper: ObjectMapper,
    private val trafficZoneSavePort: TrafficZoneSavePort,
    private val trafficZoneFindPort: TrafficZoneFindPort,
    private val trafficZoneGroupRepository: TrafficZoneGroupRepository
) : BehaviorSpec({

    lateinit var savedGroup: TrafficZoneGroup

    beforeSpec {
        savedGroup = trafficZoneGroupRepository.save(TrafficZoneGroupFixture.giveOne())
    }

    given("트래픽 Zone 정보 등록 API 요청하여") {
        val url = "/api/v1/zone"
        val memberId = 1L

        `when`("요청 'zoneId' 없이 신규 정보 등록 요청인 경우") {
            val request = V1CreateZoneRequest(
                zoneAlias = "test-zone-alias",
                threshold = 1,
                activationTime = LocalDateTime.now().convertPatternOf()
            )

            val result = mockMvc
                .post(url) {
                    header("X-KTC-Member-Id", memberId)
                    contentType = MediaType.APPLICATION_JSON
                    content = objectMapper.writeValueAsString(request)
                }
                .andDo { print() }

            then("'201 Created' 응답 정상 확인한다") {
                result.andExpect {
                    status { isCreated() }
                    content {
                        jsonPath("$.zoneId", notNullValue())
                        content { jsonPath("$.result.status", equalTo("SUCCESS")) }
                    }
                }
            }
        }

        `when`("요청 'zoneId' 포함하여 신규 정보 등록 요청인 경우") {
            val zoneId = "TEST_ZONE"
            val request = V1CreateZoneRequest(
                zoneId = zoneId,
                zoneAlias = "test-zone-alias",
                threshold = 1,
                groupId = savedGroup.groupId!!,
                activationTime = LocalDateTime.now().convertPatternOf()
            )

            val result = mockMvc
                .post(url) {
                    header("X-KTC-Member-Id", memberId)
                    contentType = MediaType.APPLICATION_JSON
                    content = objectMapper.writeValueAsString(request)
                }
                .andDo { print() }

            then("'201 Created' 응답 정상 확인한다") {
                result.andExpect {
                    status { isCreated() }
                    content {
                        jsonPath("$.zoneId", equalTo(zoneId))
                        content { jsonPath("$.result.status", equalTo("SUCCESS")) }
                    }
                }
            }
        }

        `when`("이미 등록된 'zoneId' 기준 신규 정보 등록 요청인 경우") {
            // 트래픽 Zone 테스트 데이터 등록
            val activeTrafficZone = TrafficZoneDTO(
                zoneId = "test-zone-id",
                zoneAlias = "test-zone-alias",
                threshold = 1,
                groupId = savedGroup.groupId!!,
                status = TrafficZoneStatus.ACTIVE,
                activationTime = LocalDateTime.now()
            )
            trafficZoneSavePort.create(activeTrafficZone)

            val zoneId = activeTrafficZone.zoneId
            val request = V1CreateZoneRequest(
                zoneId = zoneId,
                zoneAlias = "test-zone-alias",
                threshold = 1,
                groupId = 1,
                activationTime = LocalDateTime.now().convertPatternOf()
            )

            val result = mockMvc
                .post(url) {
                    header("X-KTC-Member-Id", memberId)
                    contentType = MediaType.APPLICATION_JSON
                    content = objectMapper.writeValueAsString(request)
                }
                .andDo { print() }

            then("'400 Bad Request' 응답 정상 확인한다") {
                result.andExpect {
                    status { isBadRequest() }
                    content {
                        jsonPath("$.result.status", equalTo("FAILED"))
                        jsonPath("$.result.code", equalTo("228_1002_103"))
                        jsonPath("$.result.message", equalTo("Traffic Zone Management Service failed. Traffic zone already exists."))
                    }
                }
            }
        }

    }

    given("트래픽 Zone 정보 단일 조회 API 요청하여") {
        val url = "/api/v1/zone"
        val notFoundZoneId = "not-found-zone-id"

        `when`("존재하지 않는 'zoneId' 기준 요청인 경우") {
            val result = mockMvc
                .get("$url/$notFoundZoneId")
                .andDo { print() }

            then("'404 Not Found' 응답 정상 확인한다") {
                result.andExpect {
                    status { isNotFound() }
                    content {
                        jsonPath("$.result.status", equalTo("FAILED"))
                        jsonPath("$.result.code", equalTo("228_1002_100"))
                        jsonPath("$.result.message", equalTo("Traffic Zone Management Service failed. Traffic zone not found."))
                    }
                }
            }
        }

        // 트래픽 Zone 테스트 데이터 등록
        val activeTrafficZone = TrafficZoneDTO(
            zoneId = "test-zone-id",
            zoneAlias = "test-zone-alias",
            threshold = 1,
            groupId = savedGroup.groupId!!,
            status = TrafficZoneStatus.ACTIVE,
            activationTime = LocalDateTime.now()
        )
        trafficZoneSavePort.create(activeTrafficZone)

        `when`("존재하는 'zoneId' 기준 요청인 경우") {
            val result = mockMvc
                .get("$url/${activeTrafficZone.zoneId}")
                .andDo { print() }

            then("'200 Ok' 응답 정상 확인한다") {
                result.andExpect {
                    status { isOk() }
                    content {
                        jsonPath("$.data.zoneId", equalTo(activeTrafficZone.zoneId))
                        jsonPath("$.data.groupId", equalTo(activeTrafficZone.groupId?.toInt()))
                        jsonPath("$.data.groupName", equalTo(activeTrafficZone.group?.name))
                        jsonPath("$.result.status", equalTo("SUCCESS"))
                    }
                }
            }
        }
    }

    given("트래픽 Zone 정보 목록 조회 API 요청하여") {
        val url = "/api/v1/zone/list"

        `when`("정상 조회 성공인 경우") {
            val result = mockMvc
                .get(url)
                .andDo { print() }

            then("'200 Ok' 응답 정상 확인한다") {
                result.andExpect {
                    status { isOk() }
                    content {
                        jsonPath("$.pageable.number", equalTo(0))
                        jsonPath("$.pageable.totalElements", greaterThanOrEqualTo(0))
                    }
                }
            }
        }
    }

    given("트래픽 Zone 정보 단일 수정 API 요청하여") {
        val url = "/api/v1/zone"
        val memberId = 1L

        val activeTrafficZone = TrafficZoneDTO(
            zoneId = "test-zone-id",
            zoneAlias = "test-zone-alias",
            threshold = 1,
            groupId = savedGroup.groupId!!,
            status = TrafficZoneStatus.ACTIVE,
            activationTime = LocalDateTime.now()
        )
        trafficZoneSavePort.create(activeTrafficZone)

        `when`("'threshold : 1000' 정보 변경 요청인 경우") {
            val request = V1UpdateZoneRequest(
                threshold = 1000
            )

            val result = mockMvc
                .put("$url/${activeTrafficZone.zoneId}") {
                    header("X-KTC-Member-Id", memberId)
                    contentType = MediaType.APPLICATION_JSON
                    content = objectMapper.writeValueAsString(request)
                }
                .andDo { print() }

            then("'200 Ok' 응답 정상 확인한다") {
                result.andExpect {
                    status { isOk() }
                    jsonPath("$.zoneId", equalTo(activeTrafficZone.zoneId))
                    content { jsonPath("$.result.status", equalTo("SUCCESS")) }
                }
            }
        }

        `when`("존재하지 않는 'zoneId' 정보 변경 요청인 경우") {
            val notFondZoneId = "not-found-test-zone-id"
            val request = V1UpdateZoneRequest(
                zoneAlias = "test-zone-alias",
                threshold = 1,
                activationTime = LocalDateTime.now().convertPatternOf()
            )

            val result = mockMvc
                .put("$url/$notFondZoneId") {
                    header("X-KTC-Member-Id", memberId)
                    contentType = MediaType.APPLICATION_JSON
                    content = objectMapper.writeValueAsString(request)
                }
                .andDo { print() }

            then("'404 Not Found' 응답 정상 확인한다") {
                result.andExpect {
                    status { isNotFound() }
                    content { jsonPath("$.result.status", equalTo("FAILED")) }
                    content { jsonPath("$.result.code", equalTo("228_1002_100")) }
                    content { jsonPath("$.result.message", equalTo("Traffic Zone Management Service failed. Traffic zone not found.")) }
                }
            }
        }

        // 트래픽 Zone 테스트 데이터 등록
        val deleteTrafficZone = TrafficZoneDTO(
            zoneId = "delete-test-zone-id",
            zoneAlias = "delete-test-zone-alias",
            threshold = 1,
            groupId = savedGroup.groupId!!,
            status = TrafficZoneStatus.DELETED,
            activationTime = LocalDateTime.now()
        )
        trafficZoneSavePort.create(deleteTrafficZone)

        `when`("이미 'DELETED' 상태 Zone 정보 변경 요청인 경우") {
            val request = V1UpdateZoneRequest(
                status = UpdateZoneStatus.ACTIVE
            )

            val result = mockMvc
                .put("$url/${deleteTrafficZone.zoneId}") {
                    header("X-KTC-Member-Id", memberId)
                    contentType = MediaType.APPLICATION_JSON
                    content = objectMapper.writeValueAsString(request)
                }
                .andDo { print() }

            then("'400 Bad Request' 응답 정상 확인한다") {
                result.andExpect {
                    status { isBadRequest() }
                    content { jsonPath("$.result.status", equalTo("FAILED")) }
                    content { jsonPath("$.result.code", equalTo("228_1002_101")) }
                    content { jsonPath("$.result.message", equalTo("Traffic Zone Management Service failed. Deleted traffic zone cannot be changed.")) }
                }
            }
        }
    }

    given("트래픽 Zone 정보 단일 삭제 API 요청하여") {
        val url = "/api/v1/zone"
        val notFoundZoneId = "not-found-zone-id"
        val memberId = 1L

        `when`("존재하지 않는 'zoneId' 기준 요청인 경우") {
            val result = mockMvc
                .delete("$url/$notFoundZoneId") {
                    header("X-KTC-Member-Id", memberId)
                }
                .andDo { print() }

            then("'404 Not Found' 응답 정상 확인한다") {
                result.andExpect {
                    status { isNotFound() }
                    content { jsonPath("$.result.status", equalTo("FAILED")) }
                    content { jsonPath("$.result.code", equalTo("228_1002_100")) }
                    content { jsonPath("$.result.message", equalTo("Traffic Zone Management Service failed. Traffic zone not found.")) }
                }
            }
        }

        // 트래픽 Zone 테스트 데이터 등록
        val activeTrafficZone = TrafficZoneDTO(
            zoneId = "test-zone-id",
            zoneAlias = "test-zone-alias",
            threshold = 1,
            groupId = savedGroup.groupId!!,
            status = TrafficZoneStatus.ACTIVE,
            activationTime = LocalDateTime.now()
        )
        trafficZoneSavePort.create(activeTrafficZone)

        `when`("존재하는 'zoneId' 기준 요청인 경우") {
            val result = mockMvc
                .delete("$url/${activeTrafficZone.zoneId}")
                .andDo { print() }

            then("'200 Ok' 응답 정상 확인한다") {
                result.andExpect {
                    status { isOk() }
                    content { jsonPath("$.result.status", equalTo("SUCCESS")) }
                }
            }

            then("요청 'zoneId' 기준 DB 정보 조회하여 'DELETED' 상태 변경 정상 확인한다") {
                val entity = trafficZoneFindPort.findTrafficZone(zoneId = activeTrafficZone.zoneId!!)
                entity.status shouldBe TrafficZoneStatus.DELETED
            }
        }
    }

})