package com.kona.ktca.infrastructure.cache

import com.kona.ktca.domain.model.TrafficZoneMonitor
import com.kona.ktca.domain.model.TrafficZoneMonitorFixture
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.time.LocalDate

class TrafficZoneMonitorCacheAdapterImplTest : StringSpec({

    val trafficZoneMonitorCacheAdapter = TrafficZoneMonitorCacheAdapterImpl()

    val trafficZoneMonitorFixture = TrafficZoneMonitorFixture()

    lateinit var saved: List<TrafficZoneMonitor>
    beforeTest {
        saved = (1..10).map { trafficZoneMonitorFixture.giveOne(it) }
        listOf("2025-06-11", "2025-06-12").forEach {
            val now = LocalDate.parse(it)
            trafficZoneMonitorCacheAdapter.saveLatestTrafficZoneMonitoring(saved, now)
        }
    }

    "Traffic Zone 모니터링 결과 Local Cache 저장 & 조회하여 정상 확인한다" {
        // given
        val today = LocalDate.parse("2025-06-12")
        val monitoring = (1..10).map { trafficZoneMonitorFixture.giveOne(it) }

        // when
        trafficZoneMonitorCacheAdapter.saveLatestTrafficZoneMonitoring(monitoring, today)

        // then
        val result = trafficZoneMonitorCacheAdapter.findLatestTrafficZoneMonitoring(today)
        result.size shouldBe 10
    }

    "Traffic Zone 모니터링 결과 Local Cache 특정 일자 key 삭제하여 정상 확인한다" {
        // given
        val today = LocalDate.parse("2025-06-12")
        val yesterday = LocalDate.parse("2025-06-11")

        // when
        trafficZoneMonitorCacheAdapter.deleteTrafficZoneMonitoring(today)

        // then
        val todayResult = trafficZoneMonitorCacheAdapter.findLatestTrafficZoneMonitoring(today)
        todayResult.size shouldBe 0
        val yesterdayResult = trafficZoneMonitorCacheAdapter.findLatestTrafficZoneMonitoring(yesterday)
        yesterdayResult.size shouldBe 10
    }

    "Traffic Zone 모니터링 결과 Local Cache 전체 삭제하여 정상 확인한다" {
        // give
        val yesterday = LocalDate.parse("2025-06-11")

        // when
        trafficZoneMonitorCacheAdapter.clearTrafficZoneMonitoring()

        // then
        val result = trafficZoneMonitorCacheAdapter.findLatestTrafficZoneMonitoring(yesterday)
        result.size shouldBe 0
    }

    "Traffic Zone 모니터링 `waitingCount: 0` Local Cache 업데이트 & 조회하여 정상 확인한다" {
        // given
        val zoneId = saved.first().zoneId
        val isZero = true

        // when
        val result = trafficZoneMonitorCacheAdapter.updateTrafficZoneWaitingCount(zoneId, isZero)

        // then
        result shouldBe trafficZoneMonitorCacheAdapter.findTrafficZoneWaitingCount(zoneId)
    }

})
