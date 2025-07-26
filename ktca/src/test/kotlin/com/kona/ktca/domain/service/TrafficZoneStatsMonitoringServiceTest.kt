package com.kona.ktca.domain.service

import com.kona.common.infrastructure.util.*
import com.kona.ktca.domain.event.TrafficZoneMonitorSavedEvent
import com.kona.ktca.domain.model.BASIC_TEST_ZONE_ID
import com.kona.ktca.domain.model.TrafficZoneStatsMonitor
import com.kona.ktca.domain.model.TrafficZoneStatsMonitorFixture
import com.kona.ktca.dto.StatsType
import com.kona.ktca.infrastructure.repository.FakeTrafficZoneStatsMonitorRepository
import io.kotest.assertions.assertSoftly
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import java.time.LocalDateTime

class TrafficZoneStatsMonitoringServiceTest : BehaviorSpec({

    val fakeTrafficZoneStatsMonitorRepository = FakeTrafficZoneStatsMonitorRepository()
    val trafficZoneStatsMonitorService = TrafficZoneStatsMonitoringService(fakeTrafficZoneStatsMonitorRepository)

    val now  = LocalDateTime.of(2025, 7, 2, 10, 12)
    val nowStatsDateMinute = now.convertPatternOf(DATE_TIME_PATTERN_yyyyMMddHHmm)
    val nowStatsDateHour = now.convertPatternOf(DATE_TIME_PATTERN_yyyyMMddHH)
    val nowStatsDateDay = now.convertPatternOf(DATE_BASIC_PATTERN)
    val nowStatsDateMonth = now.convertPatternOf(DATE_PATTERN_yyyyMM)

    lateinit var eventElement1: TrafficZoneStatsMonitor
    lateinit var eventElement2: TrafficZoneStatsMonitor
    lateinit var eventElement3: TrafficZoneStatsMonitor
    lateinit var eventElement4: TrafficZoneStatsMonitor
    lateinit var eventElement5: TrafficZoneStatsMonitor
    lateinit var eventElement6: TrafficZoneStatsMonitor
    lateinit var eventElement7: TrafficZoneStatsMonitor
    lateinit var event: TrafficZoneMonitorSavedEvent

    beforeSpec {
        eventElement1 = TrafficZoneStatsMonitorFixture.giveOne(
            threshold = 1,
            entryCountPerDay = 2,
            estimatedClearTime = 3,
            waitingCount = 4,
        )

        eventElement2 = TrafficZoneStatsMonitorFixture.giveOne(
            threshold = 4,
            entryCountPerDay = 3,
            estimatedClearTime = 2,
            waitingCount = 1,
        )

        eventElement3 = TrafficZoneStatsMonitorFixture.giveOne(
            threshold = 10,
            entryCountPerDay = 20,
            estimatedClearTime = 30,
            waitingCount = 40,
        )

        eventElement4 = TrafficZoneStatsMonitorFixture.giveOne(
            threshold = 40,
            entryCountPerDay = 30,
            estimatedClearTime = 20,
            waitingCount = 10,
        )

        eventElement5 = TrafficZoneStatsMonitorFixture.giveOne(
            threshold = 100,
            entryCountPerDay = 200,
            estimatedClearTime = 300,
            waitingCount = 400,
        )

        eventElement6 = TrafficZoneStatsMonitorFixture.giveOne(
            threshold = 400,
            entryCountPerDay = 300,
            estimatedClearTime = 200,
            waitingCount = 100,
        )

        eventElement7 = TrafficZoneStatsMonitorFixture.giveOne(
            threshold = 1000,
            entryCountPerDay = 2000,
            estimatedClearTime = 3000,
            waitingCount = 4000,
        )
    }

    Given("통계 모니터링 서비스가 있을 때") {

        event = TrafficZoneMonitorSavedEvent(listOf(eventElement1))
        trafficZoneStatsMonitorService.applySaveEvent(now, event)

        event = TrafficZoneMonitorSavedEvent(listOf(eventElement2))
        trafficZoneStatsMonitorService.applySaveEvent(now.plusMinutes(1), event)

        event = TrafficZoneMonitorSavedEvent(listOf(eventElement3))
        trafficZoneStatsMonitorService.applySaveEvent(now.plusMinutes(2), event)

        event = TrafficZoneMonitorSavedEvent(listOf(eventElement4))
        trafficZoneStatsMonitorService.applySaveEvent(now.plusHours(1), event)

        event = TrafficZoneMonitorSavedEvent(listOf(eventElement5))
        trafficZoneStatsMonitorService.applySaveEvent(now.plusHours(2), event)

        event = TrafficZoneMonitorSavedEvent(listOf(eventElement6))
        trafficZoneStatsMonitorService.applySaveEvent(now.plusDays(1), event)

        event = TrafficZoneMonitorSavedEvent(listOf(eventElement7))
        trafficZoneStatsMonitorService.applySaveEvent(now.plusMonths(1), event)

        When("분 단위로 특정 Zone의 통계 정보를 요청하면") {
            val result = trafficZoneStatsMonitorService.statsMonitor(
                zoneId = BASIC_TEST_ZONE_ID,
                statsType = StatsType.MINUTE,
                startDate = now.convertPatternAndAddUnitOf(DATE_TIME_PATTERN_yyyyMMddHHmm, 1),
                endDate = now.convertPatternAndAddUnitOf(DATE_TIME_PATTERN_yyyyMMddHHmm, 2)
            )

            Then("해당 Zone의 분 단위 통계 정보가 반환되어야 한다.") {
                assertSoftly {
                    result[0].statsType shouldBe StatsType.MINUTE
                    result[0].statsDate shouldBe now.convertPatternAndAddUnitOf(DATE_TIME_PATTERN_yyyyMMddHHmm, 1)
                    result[0].maxThreshold shouldBe 4
                    result[0].totalEntryCount shouldBe 1
                    result[0].maxEstimatedClearTime shouldBe 2
                    result[0].maxWaitingCount shouldBe 1

                    result[1].statsType shouldBe StatsType.MINUTE
                    result[1].statsDate shouldBe now.convertPatternAndAddUnitOf(DATE_TIME_PATTERN_yyyyMMddHHmm, 2)
                    result[1].maxThreshold shouldBe 10
                    result[1].totalEntryCount shouldBe 17
                    result[1].maxEstimatedClearTime shouldBe 30
                    result[1].maxWaitingCount shouldBe 40
                }
            }
        }

        When("시간 단위로 특정 Zone의 통계 정보를 요청하면") {
            val result = trafficZoneStatsMonitorService.statsMonitor(
                zoneId = BASIC_TEST_ZONE_ID,
                statsType = StatsType.HOUR,
                startDate = now.convertPatternAndAddUnitOf(DATE_TIME_PATTERN_yyyyMMddHH, 1),
                endDate = now.convertPatternAndAddUnitOf(DATE_TIME_PATTERN_yyyyMMddHH, 2)
            )
            Then("해당 Zone의 시간 단위 통계 정보가 반환되어야 한다.") {
                assertSoftly {
                    result[0].statsType shouldBe StatsType.HOUR
                    result[0].statsDate shouldBe now.convertPatternAndAddUnitOf(DATE_TIME_PATTERN_yyyyMMddHH, 1)
                    result[0].maxThreshold shouldBe 40
                    result[0].totalEntryCount shouldBe 10
                    result[0].maxEstimatedClearTime shouldBe 20
                    result[0].maxWaitingCount shouldBe 10

                    result[1].statsType shouldBe StatsType.HOUR
                    result[1].statsDate shouldBe now.convertPatternAndAddUnitOf(DATE_TIME_PATTERN_yyyyMMddHH, 2)
                    result[1].maxThreshold shouldBe 100
                    result[1].totalEntryCount shouldBe 170
                    result[1].maxEstimatedClearTime shouldBe 300
                    result[1].maxWaitingCount shouldBe 400
                }
            }
        }

        When("일 단위로 특정 Zone의 통계 정보를 요청하면") {
            val result = trafficZoneStatsMonitorService.statsMonitor(
                zoneId = BASIC_TEST_ZONE_ID,
                statsType = StatsType.DAY,
                startDate = nowStatsDateDay,
                endDate = now.convertPatternAndAddUnitOf(DATE_BASIC_PATTERN, 1)
            )
            Then("해당 Zone의 일 단위 통계 정보가 반환되어야 한다.") {
                assertSoftly {
                    result[0].statsType shouldBe StatsType.DAY
                    result[0].statsDate shouldBe nowStatsDateDay
                    result[0].maxThreshold shouldBe 100
                    result[0].totalEntryCount shouldBe 200
                    result[0].maxEstimatedClearTime shouldBe 300
                    result[0].maxWaitingCount shouldBe 400

                    result[1].statsType shouldBe StatsType.DAY
                    result[1].statsDate shouldBe now.convertPatternAndAddUnitOf(DATE_BASIC_PATTERN, 1)
                    result[1].maxThreshold shouldBe 400
                    result[1].totalEntryCount shouldBe 300
                    result[1].maxEstimatedClearTime shouldBe 200
                    result[1].maxWaitingCount shouldBe 100
                }
            }
        }

        When("월 단위로 특정 Zone의 통계 정보를 요청하면") {
            val result = trafficZoneStatsMonitorService.statsMonitor(
                zoneId = BASIC_TEST_ZONE_ID,
                statsType = StatsType.MONTH,
                startDate = nowStatsDateMonth,
                endDate = now.convertPatternAndAddUnitOf(DATE_PATTERN_yyyyMM, 1)
            )
            Then("해당 Zone의 월 단위 통계 정보가 반환되어야 한다.") {
                assertSoftly {
                    result[0].statsType shouldBe StatsType.MONTH
                    result[0].statsDate shouldBe nowStatsDateMonth
                    result[0].maxThreshold shouldBe 400
                    result[0].totalEntryCount shouldBe 500
                    result[0].maxEstimatedClearTime shouldBe 300
                    result[0].maxWaitingCount shouldBe 400

                    result[1].statsType shouldBe StatsType.MONTH
                    result[1].statsDate shouldBe now.convertPatternAndAddUnitOf(DATE_PATTERN_yyyyMM, 1)
                    result[1].maxThreshold shouldBe 1000
                    result[1].totalEntryCount shouldBe 2000
                    result[1].maxEstimatedClearTime shouldBe 3000
                    result[1].maxWaitingCount shouldBe 4000
                }
            }
        }
        
    }
})