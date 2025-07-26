package com.kona.ktca.infrastructure.repository

import com.kona.common.infrastructure.util.DATE_TIME_PATTERN_yyyyMMddHHmm
import com.kona.common.infrastructure.util.convertPatternAndAddUnitOf
import com.kona.ktca.domain.dto.TrafficZoneStatsMonitorDTO
import com.kona.ktca.domain.model.BASIC_TEST_ZONE_ALIAS
import com.kona.ktca.domain.model.BASIC_TEST_ZONE_ID
import com.kona.ktca.domain.model.TrafficZoneStatsMonitorFixture
import com.kona.ktca.dto.StatsType
import com.kona.ktca.infrastructure.repository.entity.TrafficZoneStatsMonitorEntity
import com.kona.ktca.infrastructure.repository.jpa.TrafficZoneStatsMonitorJpaRepository
import io.kotest.assertions.assertSoftly
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.slf4j.LoggerFactory
import org.springframework.boot.test.context.SpringBootTest
import java.time.LocalDateTime

@SpringBootTest
class TrafficZoneStatsMonitorRepositoryImplTest(
    private val trafficZoneStatsMonitorJpaRepository: TrafficZoneStatsMonitorJpaRepository
) : StringSpec({

    val log = LoggerFactory.getLogger(this::class.java)
    val trafficZoneStatsMonitorRepositoryImpl = TrafficZoneStatsMonitorRepositoryImpl(trafficZoneStatsMonitorJpaRepository)

    beforeSpec {

    }

    "TrafficZoneStatsMonitorEntity DB 저장 처리 결과 정상 확인한다" {
        val basicDateTime  = LocalDateTime.of(2025, 7, 2, 10, 12)

        val secondTimeString    = basicDateTime.convertPatternAndAddUnitOf(DATE_TIME_PATTERN_yyyyMMddHHmm, 0)
        val thirdTimeString     = basicDateTime.convertPatternAndAddUnitOf(DATE_TIME_PATTERN_yyyyMMddHHmm, 1)
        val firstTimeString     = basicDateTime.convertPatternAndAddUnitOf(DATE_TIME_PATTERN_yyyyMMddHHmm, -60)

        secondTimeString    shouldBe "202507021012"
        thirdTimeString     shouldBe "202507021013"
        firstTimeString     shouldBe "202507020912"

        val saved = trafficZoneStatsMonitorRepositoryImpl.saveAll(
            listOf(
                TrafficZoneStatsMonitorFixture.giveOne(statsDate = secondTimeString),
                TrafficZoneStatsMonitorFixture.giveOne(statsDate = thirdTimeString),
                TrafficZoneStatsMonitorFixture.giveOne(
                    statsDate = firstTimeString,
                    maxThreshold = 1,
                    totalEntryCount = 2,
                    maxWaitingCount = 3,
                    maxEstimatedClearTime = 4,
                ),
            )
        )

        log.info("\n\n saved : $saved \n\n")

        val find = trafficZoneStatsMonitorRepositoryImpl.findAllByPredicate(
            TrafficZoneStatsMonitorDTO(
                zoneId = BASIC_TEST_ZONE_ID,
                statsType = StatsType.MINUTE,
                startDate   = "202507020912",   // firstTimeString
                endDate     = "202507021013"    // thirdTimeString
            )
        )

        log.info("\n\n $find \n\n")

        saved[2] shouldBe find[0]
        saved[0] shouldBe find[1]
        saved[1] shouldBe find[2]

        println(TrafficZoneStatsMonitorEntity.of(find[0]))
        println(TrafficZoneStatsMonitorEntity.of(find[1]))
        println(TrafficZoneStatsMonitorEntity.of(find[2]))

        assertSoftly {
            find[0].zoneId shouldBe BASIC_TEST_ZONE_ID
            find[0].statsDate shouldBe firstTimeString
            find[0].zoneAlias shouldBe BASIC_TEST_ZONE_ALIAS
            find[0].statsType shouldBe StatsType.MINUTE
            find[0].maxThreshold shouldBe 1
            find[0].totalEntryCount shouldBe 2
            find[0].maxWaitingCount shouldBe 3
            find[0].maxEstimatedClearTime shouldBe 4
        }
    }
}) {
}