package com.kona.ktca.presentation.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.kona.common.infrastructure.enumerate.TrafficZoneGroupStatus
import com.kona.ktca.domain.port.outbound.TrafficZoneGroupRepository
import com.kona.ktca.dto.V1CreateZoneGroupRequest
import com.kona.ktca.dto.V1UpdateZoneGroupRequest
import com.kona.ktca.dto.V1ZoneGroupData
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldHaveAtLeastSize
import io.kotest.matchers.shouldBe
import org.hamcrest.Matchers.*
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.*

@AutoConfigureMockMvc
@SpringBootTest
class V1ZoneGroupManagementControllerTest(
    private val mockMvc: MockMvc,
    private val objectMapper: ObjectMapper,
    private val trafficZoneGroupRepository: TrafficZoneGroupRepository
) : BehaviorSpec({

    given("트래픽 Zone 그룹 정보 단일 등록 API 요청하여") {
        val url = "/api/v1/zone/group"
        val request = V1CreateZoneGroupRequest(groupName = "테스트 그룹")

        `when` ("정상 등록 성공인 경우") {
            val result = mockMvc
                .post(url) {
                    contentType = MediaType.APPLICATION_JSON
                    content = objectMapper.writeValueAsString(request)
                }
                .andDo { print() }

            then("'201 Created' 응답 결과 정상 확인한다") {
                result.andExpect {
                    status { isCreated() }
                    content {
                        jsonPath("$.groupId") { exists() }
                    }
                }
            }

            then("DB 신규 등록 결과 정상 확인한다") {
                val entity = trafficZoneGroupRepository.findAllByStatus(status = TrafficZoneGroupStatus.ACTIVE)
                entity shouldHaveAtLeastSize 1
            }
        }
    }

    given("트래픽 Zone 그룹 정보 목록 조회 API 요청하여") {
        val url = "/api/v1/zone/group/list"

        trafficZoneGroupRepository.saveNextOrder("테스트 그룹-1")
        trafficZoneGroupRepository.saveNextOrder("테스트 그룹-2")

        `when`("정상 조회 성공인 경우") {
            val result = mockMvc
                .get(url)
                .andDo { print() }

            then("'200 Ok' 응답 결과 정상 확인한다") {
                result.andExpect {
                    status { isOk() }
                    content {
                        jsonPath("$.content", hasSize<V1ZoneGroupData>(greaterThan(2)))
                    }
                }
            }
        }
    }
    
    given("트래픽 Zone 그룹 정보 단일 수정 API 요청하여") {
        val url = "/api/v1/zone/group"
        val notExistGroupId = 0
        val request = V1UpdateZoneGroupRequest(groupOrder = 10)

        `when`("요청 'groupId' 기준 일치한 정보 없는 경우") {
            val result = mockMvc
                .put("$url/$notExistGroupId") {
                    contentType = MediaType.APPLICATION_JSON
                    content = objectMapper.writeValueAsString(request)
                }
                .andDo { print() }
            
            then("'404 Not Found' 응답 결과 정상 확인한다") {
                result.andExpect {
                    status { isNotFound() }
                    content { jsonPath("$.result.status", equalTo("FAILED")) }
                    content { jsonPath("$.result.code", equalTo("228_1005_104")) }
                    content { jsonPath("$.result.message", equalTo("Traffic Zone Group Management Service failed. Traffic zone group not found.")) }
                }
            }
        }

        val group = trafficZoneGroupRepository.saveNextOrder("테스트 그룹")
        val groupId = group.groupId!!

        `when`("요청 'groupId' 기준 일치한 정보 변경하는 경우") {
            val result = mockMvc
                .put("$url/$groupId") {
                    contentType = MediaType.APPLICATION_JSON
                    content = objectMapper.writeValueAsString(request)
                }
                .andDo { print() }

            then("'200 Ok' 응답 결과 정상 확인한다") {
                result.andExpect {
                    status { isOk() }
                    content {
                        jsonPath("$.groupId", equalTo(groupId.toInt()))
                    }
                }
            }

            then("DB 'order' 변경 결과 정상 확인한다") {
                val entity = trafficZoneGroupRepository.findByGroupIdAndStatus(groupId = groupId, status = TrafficZoneGroupStatus.ACTIVE)!!
                entity.order shouldBe request.groupOrder
            }
        }
    }

    given("트래픽 Zone 그룹 정보 단일 삭제 API 요청하여") {
        val url = "/api/v1/zone/group"
        val notExistGroupId = 0

        `when`("요청 'groupId' 기준 일치한 정보 없는 경우") {
            val result = mockMvc
                .delete("$url/$notExistGroupId")
                .andDo { print() }

            then("'404 Not Found' 응답 결과 정상 확인한다") {
                result.andExpect {
                    status { isNotFound() }
                    content { jsonPath("$.result.status", equalTo("FAILED")) }
                    content { jsonPath("$.result.code", equalTo("228_1005_104")) }
                    content { jsonPath("$.result.message", equalTo("Traffic Zone Group Management Service failed. Traffic zone group not found.")) }
                }
            }
        }

        val group = trafficZoneGroupRepository.saveNextOrder("테스트 그룹")
        val groupId = group.groupId!!

        `when`("요청 'groupId' 기준 일치한 정보 변경하는 경우") {
            val result = mockMvc
                .delete("$url/$groupId")
                .andDo { print() }

            then("'200 Ok' 응답 결과 정상 확인한다") {
                result.andExpect {
                    status { isOk() }
                }
            }

            then("DB 'status: DELETED' 변경 결과 정상 확인한다") {
                val entity = trafficZoneGroupRepository.findByGroupIdAndStatus(groupId = groupId, status = TrafficZoneGroupStatus.DELETED)!!
                entity.status shouldBe TrafficZoneGroupStatus.DELETED
            }
        }
    }

})
