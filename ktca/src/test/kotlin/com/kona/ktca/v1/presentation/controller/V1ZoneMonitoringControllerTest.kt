package com.kona.ktca.v1.presentation.controller

import io.kotest.core.spec.style.BehaviorSpec
import org.hamcrest.Matchers.greaterThanOrEqualTo
import org.hamcrest.Matchers.notNullValue
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@AutoConfigureMockMvc
@SpringBootTest
class V1ZoneMonitoringControllerTest(
    private val mockMvc: MockMvc
) : BehaviorSpec({

    given("전체 트래픽 제어 Zone 모니터링 요청되어") {
        val url = "/api/v1/zone/monitoring"

        `when`("요청 결과 성공인 경우") {
            val result = mockMvc
                .get(url)
                .andDo { print() }

            then("'200 Ok' 응답 정상 확인한다") {
                result
                    .andExpect {
                        status { isOk() }
                        content {
                            jsonPath("$.content", notNullValue())
                            jsonPath("$.content.size()", greaterThanOrEqualTo(0))
                        }
                    }
            }
        }
    }

})