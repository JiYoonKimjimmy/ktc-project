package com.kona.common.enumerate

import com.kona.common.infrastructure.enumerate.TrafficCacheKey
import com.kona.common.infrastructure.enumerate.TrafficCacheKey.*
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class TrafficCacheKeyTest : StringSpec({

    "'TEST_ZONE' zoneId 기준 TrafficCacheKey 전체 생성 결과 정상 확인한다" {
        // given
        val zoneId = "TEST_ZONE"

        // when
        val result = TrafficCacheKey.generate(zoneId)

        // then
        result[QUEUE]                   shouldBe "ktc:{TEST_ZONE}:queue"
        result[QUEUE_STATUS]            shouldBe "ktc:{TEST_ZONE}:queue-status"
        result[THRESHOLD]               shouldBe "ktc:{TEST_ZONE}:threshold"
        result[ENTRY_WINDOW]            shouldBe "ktc:{TEST_ZONE}:entry-window"
        result[ENTRY_SLOT]              shouldBe "ktc:{TEST_ZONE}:entry-slot"
        result[ENTRY_COUNT]             shouldBe "ktc:{TEST_ZONE}:entry-count"
        result[TOKEN_LAST_POLLING_TIME] shouldBe "ktc:{TEST_ZONE}:token-last-polling-time"
    }

    "'TEST_ZONE' zoneId 기준 TrafficCacheKey enum 'key' 목록 생성 결과 정상 확인한다" {
        // given
        val zoneId = "TEST_ZONE"

        // when
        val result = TrafficCacheKey.getTrafficControlKeys(zoneId).map { it.value }

        // then
        val expected = listOf(
            "ktc:{TEST_ZONE}:queue",
            "ktc:{TEST_ZONE}:queue-status",
            "ktc:{TEST_ZONE}:threshold",
            "ktc:{TEST_ZONE}:entry-window",
            "ktc:{TEST_ZONE}:entry-slot",
            "ktc:{TEST_ZONE}:entry-count",
            "ktc:{TEST_ZONE}:token-last-polling-time",
        )
        result shouldBe expected
    }

})