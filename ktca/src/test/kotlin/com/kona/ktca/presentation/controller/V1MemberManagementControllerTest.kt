package com.kona.ktca.presentation.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.kona.common.infrastructure.util.convertPatternOf
import com.kona.ktca.domain.model.Member
import com.kona.ktca.domain.model.MemberFixture
import com.kona.ktca.domain.port.outbound.MemberRepository
import com.kona.ktca.dto.MemberRole
import com.kona.ktca.dto.V1CreateMemberRequest
import com.kona.ktca.dto.V1MemberData
import com.kona.ktca.dto.V1UpdateMemberRequest
import io.kotest.core.spec.style.BehaviorSpec
import org.hamcrest.Matchers.*
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put

@AutoConfigureMockMvc
@SpringBootTest
class V1MemberManagementControllerTest(
    private val mockMvc: MockMvc,
    private val objectMapper: ObjectMapper,
    private val memberRepository: MemberRepository
) : BehaviorSpec({

    val memberFixture = MemberFixture()
    lateinit var saved: Member

    beforeSpec {
        val member = memberFixture.giveOne()
        saved = memberRepository.save(member)
    }
    
    given("관리자 단일 등록 API 요청하여") {
        val url = "/api/v1/member"

        val invalidRequest = V1CreateMemberRequest(
            loginId = saved.loginId,
            password = "password",
            name = "name",
            email = "email",
            team = "team",
            role = MemberRole.VIEWER,
            requestMemberId = 1
        )

        `when`("요청 'loginId' 이미 등록된 정보 등록 요청인 경우") {
            val result = mockMvc
                .post(url) {
                    contentType = MediaType.APPLICATION_JSON
                    content = objectMapper.writeValueAsString(invalidRequest)
                }
                .andDo { print() }

            then("'400 Bad Request' 응답 결과 정상 확인한다") {
                result.andExpect {
                    status { isBadRequest() }
                    content {
                        content { jsonPath("$.result.status", equalTo("FAILED")) }
                        content { jsonPath("$.result.code", equalTo("228_1004_201")) }
                        content { jsonPath("$.result.message", equalTo("Member Management Service failed. Member loginId already exists.")) }
                    }
                }
            }
        }

        val request = V1CreateMemberRequest(
            loginId = "loginId",
            password = "password",
            name = "name",
            email = "email",
            team = "team",
            role = MemberRole.VIEWER,
            requestMemberId = 1
        )

        `when`("신규 정보 등록 요청인 경우") {
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
                        jsonPath("memberId") { notNullValue() }
                        content { jsonPath("$.result.status", equalTo("SUCCESS")) }
                    }
                }
            }
        }
    }
    
    given("관리자 단일 조회 API 요청하여") {
        val url = "/api/v1/member"
        val notExistsMemberId = 0

        `when`("'memberId' 기준 동일한 정보가 없는 경우") {
            val result = mockMvc
                .get("$url?memberId=$notExistsMemberId")
                .andDo { print() }
            
            then("'404 Not Found' 응답 결과 정상 확인한다") {
                result.andExpect {
                    status { isNotFound() }
                    content {
                        content { jsonPath("$.result.status", equalTo("FAILED")) }
                        content { jsonPath("$.result.code", equalTo("228_1004_200")) }
                        content { jsonPath("$.result.message", equalTo("Member Management Service failed. Member not found.")) }
                    }
                }
            }
        }

        val notExistsLoginId = "notExistsLoginId"

        `when`("'loginId' 기준 동일한 정보가 없는 경우") {
            val result = mockMvc
                .get("$url?loginId=$notExistsLoginId")
                .andDo { print() }

            then("'404 Not Found' 응답 결과 정상 확인한다") {
                result.andExpect {
                    status { isNotFound() }
                    content {
                        content { jsonPath("$.result.status", equalTo("FAILED")) }
                        content { jsonPath("$.result.code", equalTo("228_1004_200")) }
                        content { jsonPath("$.result.message", equalTo("Member Management Service failed. Member not found.")) }
                    }
                }
            }
        }

        val memberId = saved.memberId

        `when`("'memberId' 기준 동일한 정보가 있는 경우") {
            val result = mockMvc
                .get("$url?memberId=$memberId")
                .andDo { print() }

            then("'200 Ok' 응답 결과 정상 확인한다") {
                result.andExpect {
                    status { isOk() }
                    content {
                        content { jsonPath("$.data.memberId", equalTo(memberId?.toInt())) }
                        content { jsonPath("$.data.loginId", equalTo(saved.loginId)) }
                        content { jsonPath("$.data.password", equalTo(saved.password)) }
                        content { jsonPath("$.data.name", equalTo(saved.name)) }
                        content { jsonPath("$.data.email", equalTo(saved.email)) }
                        content { jsonPath("$.data.team", equalTo(saved.team)) }
                        content { jsonPath("$.data.role", equalTo(saved.role.name)) }
                        content { jsonPath("$.data.status", equalTo(saved.status.name)) }
                        content { jsonPath("$.data.lastLoginAt", equalTo(saved.lastLoginAt.convertPatternOf())) }
                    }
                }
            }
        }

        val loginId = saved.loginId

        `when`("'loginId' 기준 동일한 정보가 있는 경우") {
            val result = mockMvc
                .get("$url?loginId=$loginId")
                .andDo { print() }

            then("'200 Ok' 응답 결과 정상 확인한다") {
                result.andExpect {
                    status { isOk() }
                    content {
                        content { jsonPath("$.data.memberId", equalTo(memberId?.toInt())) }
                        content { jsonPath("$.data.loginId", equalTo(saved.loginId)) }
                        content { jsonPath("$.data.password", equalTo(saved.password)) }
                        content { jsonPath("$.data.name", equalTo(saved.name)) }
                        content { jsonPath("$.data.email", equalTo(saved.email)) }
                        content { jsonPath("$.data.team", equalTo(saved.team)) }
                        content { jsonPath("$.data.role", equalTo(saved.role.name)) }
                        content { jsonPath("$.data.status", equalTo(saved.status.name)) }
                        content { jsonPath("$.data.lastLoginAt", equalTo(saved.lastLoginAt.convertPatternOf())) }
                    }
                }
            }
        }
    }

    given("관리자 목록 조회 API 요청하여") {
        val url = "/api/v1/member/list"
        val notExistsTeam = "notExistsTeam"

        `when`("요청 'team' 기준 동일한 정보가 없는 경우") {
            val result = mockMvc
                .get("$url?team=$notExistsTeam")
                .andDo { print() }

            then("'200 Ok' 응답 결과 정상 확인한다") {
                result.andExpect {
                    status { isOk() }
                    content {
                        content { jsonPath("$.pageable.first", equalTo(true)) }
                        content { jsonPath("$.pageable.last", equalTo(true)) }
                        content { jsonPath("$.pageable.number", equalTo(0)) }
                        content { jsonPath("$.pageable.numberOfElements", equalTo(0)) }
                        content { jsonPath("$.pageable.size", equalTo(20)) }
                        content { jsonPath("$.pageable.totalPages", equalTo(0)) }
                        content { jsonPath("$.pageable.totalElements", equalTo(0)) }
                        content { jsonPath("$.content", empty<V1MemberData>()) }
                    }
                }
            }
        }

        val team = saved.team

        `when`("요청 'team' 기준 동일한 정보 목록 있는 경우") {
            val result = mockMvc
                .get("$url?team=$team")
                .andDo { print() }

            then("'200 Ok' 응답 결과 정상 확인한다") {
                result.andExpect {
                    status { isOk() }
                    content {
                        content { jsonPath("$.pageable.first", equalTo(true)) }
                        content { jsonPath("$.pageable.last", equalTo(true)) }
                        content { jsonPath("$.pageable.number", equalTo(0)) }
                        content { jsonPath("$.pageable.numberOfElements", equalTo(1)) }
                        content { jsonPath("$.pageable.size", equalTo(20)) }
                        content { jsonPath("$.pageable.totalPages", equalTo(1)) }
                        content { jsonPath("$.pageable.totalElements", equalTo(1)) }
                        content { jsonPath("$.content[0].memberId", equalTo(saved.memberId?.toInt())) }
                        content { jsonPath("$.content[0].loginId", equalTo(saved.loginId)) }
                        content { jsonPath("$.content[0].password", equalTo(saved.password)) }
                        content { jsonPath("$.content[0].name", equalTo(saved.name)) }
                        content { jsonPath("$.content[0].email", equalTo(saved.email)) }
                        content { jsonPath("$.content[0].team", equalTo(saved.team)) }
                        content { jsonPath("$.content[0].role", equalTo(saved.role.name)) }
                        content { jsonPath("$.content[0].status", equalTo(saved.status.name)) }
                        content { jsonPath("$.content[0].lastLoginAt", equalTo(saved.lastLoginAt.convertPatternOf())) }
                    }
                }
            }
        }
    }

    given("관리자 단일 수정 API 요청하여") {
        val url = "/api/v1/member"

        val notExistsMemberId = 0
        val notExistsMemberRequest = V1UpdateMemberRequest(loginId = "updateLoginId")

        `when`("요청 'memberId' 기준 동일한 정보가 없는 경우") {
            val result = mockMvc
                .put("$url/$notExistsMemberId") {
                    contentType = MediaType.APPLICATION_JSON
                    content = objectMapper.writeValueAsString(notExistsMemberRequest)
                }

            then("'404 Not Found' 응답 결과 정상 확인한다") {
                result.andExpect {
                    status { isNotFound() }
                    content {
                        content { jsonPath("$.result.status", equalTo("FAILED")) }
                        content { jsonPath("$.result.code", equalTo("228_1004_200")) }
                        content { jsonPath("$.result.message", equalTo("Member Management Service failed. Member not found.")) }
                    }
                }
            }
        }

        val memberId = saved.memberId
        val duplicatedLogindIdRequest = V1UpdateMemberRequest(loginId = saved.loginId)

        `when` ("요청 'memberId' 기준 관리자 정보 동일 'loginId' 변경 요청인 경우") {
            val result = mockMvc
                .put("$url/$memberId") {
                    contentType = MediaType.APPLICATION_JSON
                    content = objectMapper.writeValueAsString(duplicatedLogindIdRequest)
                }

            then("'400 Bad Request' 응답 결과 정상 확인한다") {
                result.andExpect {
                    status { isBadRequest() }
                    content {
                        content { jsonPath("$.result.status", equalTo("FAILED")) }
                        content { jsonPath("$.result.code", equalTo("228_1004_201")) }
                        content { jsonPath("$.result.message", equalTo("Member Management Service failed. Member loginId already exists.")) }
                    }
                }
            }
        }

        val request = V1UpdateMemberRequest(loginId = "newLoginId")

        `when` ("요청 'memberId' 기준 관리자 정보 신규 'loginId' 변경 요청인 경우") {
            val result = mockMvc
                .put("$url/$memberId") {
                    contentType = MediaType.APPLICATION_JSON
                    content = objectMapper.writeValueAsString(request)
                }

            then("'200 Ok' 응답 결과 정상 확인한다") {
                result.andExpect {
                    status { isOk() }
                    content {
                        content { jsonPath("$.memberId", equalTo(1)) }
                    }
                }
            }
        }
    }

})
