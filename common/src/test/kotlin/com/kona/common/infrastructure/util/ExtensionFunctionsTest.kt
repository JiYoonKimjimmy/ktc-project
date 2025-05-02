package com.kona.common.infrastructure.util

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldHaveLength
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking

class ExtensionFunctionsTest : StringSpec({

    "CorrelationId 생성 결과 정상 확인한다" {
        // when
        val result = getCorrelationId()

        // then
        result shouldNotBe null
        result shouldHaveLength 20
    }

    "동시 생성된 CorrelationId 생성하여 각각 다른 값 결과 정상 확인한다" {
        // when
        val result1 = runBlocking { async { getCorrelationId() }.await() }
        val result2 = runBlocking { async { getCorrelationId() }.await() }

        // then
        result1 shouldNotBe result2
    }

})