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
        result[TRAFFIC_ZQUEUE] shouldBe "ktc:{TEST_ZONE}:zqueue"
        result[TRAFFIC_TOKENS] shouldBe "ktc:{TEST_ZONE}:bucket"
        result[TRAFFIC_THRESHOLD] shouldBe "ktc:{TEST_ZONE}:threshold"
        result[TRAFFIC_LAST_REFILL_TIME] shouldBe "ktc:{TEST_ZONE}:last_refill_time"
        result[TRAFFIC_LAST_ENTRY_TIME] shouldBe "ktc:{TEST_ZONE}:last_entry_time"
    }

    "'TEST_ZONE' zoneId 기준 TrafficCacheKey enum 'key' 목록 생성 결과 정상 확인한다" {
        // given
        val zoneId = "TEST_ZONE"

        // when
        val result = TrafficCacheKey.generateKeys(zoneId)

        // then
        val expected = listOf(
            "ktc:{TEST_ZONE}:zqueue",
            "ktc:{TEST_ZONE}:bucket",
            "ktc:{TEST_ZONE}:last_refill_time",
            "ktc:{TEST_ZONE}:last_entry_time",
            "ktc:{TEST_ZONE}:threshold",
        )
        result shouldContainAll expected
    }

})