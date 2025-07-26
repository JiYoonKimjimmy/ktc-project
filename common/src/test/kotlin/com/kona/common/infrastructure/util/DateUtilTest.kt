package com.kona.common.infrastructure.util

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class DateUtilTest : StringSpec({

    "매월 1일 정보 생성 결과 정상 확인한다" {
        // given
        val yyyyMM = "202507"

        // when
        val result = convertFirstDayOfMonth(yyyyMM)

        // then
        result shouldBe "20250701"
    }

    "매월 마지막 일자 정보 생성 결과 정상 확인한다" {
        // given
        val yyyyMM = "202507"

        // when
        val result = convertLastDayOfMonth(yyyyMM)

        // then
        result shouldBe "20250731"
    }

})
