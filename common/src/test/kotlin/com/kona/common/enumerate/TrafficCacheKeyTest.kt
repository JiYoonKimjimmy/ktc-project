package com.kona.common.enumerate

import com.kona.common.infrastructure.enumerate.TrafficCacheKey
import com.kona.common.infrastructure.enumerate.TrafficCacheKey.*
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.shouldBe

class TrafficCacheKeyTest : StringSpec({

    "'TEST_ZONE' zoneId 기준 TrafficCacheKey 전체 생성 결과 정상 확인한다" {
        // given
        val zoneId = "TEST_ZONE"

        // when
        val result = TrafficCacheKey.generate(zoneId)

        // then
        result[QUEUE] shouldBe "ktc:{TEST_ZONE}:queue"
        result[QUEUE_CURSOR] shouldBe "ktc:{TEST_ZONE}:queue_cursor"
        result[BUCKET] shouldBe "ktc:{TEST_ZONE}:bucket"
        result[BUCKET_REFILL_TIME] shouldBe "ktc:{TEST_ZONE}:bucket_refill_time"
        result[THRESHOLD] shouldBe "ktc:{TEST_ZONE}:threshold"
    }

    "'TEST_ZONE' zoneId 기준 TrafficCacheKey enum 'key' 목록 생성 결과 정상 확인한다" {
        // given
        val zoneId = "TEST_ZONE"

        // when
        val result = TrafficCacheKey.getTrafficControlKeys(zoneId).map { it.value }

        // then
        val expected = listOf(
            "ktc:{TEST_ZONE}:queue",
            "ktc:{TEST_ZONE}:queue_cursor",
            "ktc:{TEST_ZONE}:bucket",
            "ktc:{TEST_ZONE}:bucket_refill_time",
            "ktc:{TEST_ZONE}:threshold"
        )
        result shouldContainAll expected
    }

})