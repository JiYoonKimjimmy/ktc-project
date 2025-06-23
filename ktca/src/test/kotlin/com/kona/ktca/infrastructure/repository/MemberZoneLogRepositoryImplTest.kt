package com.kona.ktca.infrastructure.repository

import com.kona.common.infrastructure.enumerate.MemberLogType
import com.kona.ktca.domain.dto.MemberLogDTO
import com.kona.ktca.domain.dto.PageableDTO
import com.kona.ktca.domain.model.MemberLog
import com.kona.ktca.domain.model.MemberLogFixture
import com.kona.ktca.domain.model.TrafficZoneFixture
import com.kona.ktca.domain.port.outbound.MemberZoneLogRepository
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.ints.shouldBeGreaterThanOrEqual
import io.kotest.matchers.longs.shouldBeGreaterThanOrEqual
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.springframework.boot.test.context.SpringBootTest
import java.time.LocalDate
import java.time.LocalTime

@SpringBootTest
class MemberZoneLogRepositoryImplTest(
    private val memberZoneLogRepository: MemberZoneLogRepository
) : StringSpec({

    val memberLogFixture = MemberLogFixture()
    val trafficZoneFixture = TrafficZoneFixture()

    lateinit var saved: MemberLog

    beforeSpec {
        val memberId = 1L
        val type = MemberLogType.TRAFFIC_ZONE_CREATED
        val zone = trafficZoneFixture.giveOne()
        val log = memberLogFixture.giveOne(memberId, type, zone)
        saved = memberZoneLogRepository.save(log)
    }

    "관리자 Member log 저장 처리 결과 정상 확인한다" {
        // given
        val memberId = 1L
        val type = MemberLogType.TRAFFIC_ZONE_UPDATED
        val zone = trafficZoneFixture.giveOne(zoneId = saved.zoneLog.zoneId)
        val log = memberLogFixture.giveOne(memberId, type, zone)

        // when
        val result = memberZoneLogRepository.save(log)

        // then
        result.logId shouldNotBe null
        result.memberId shouldBe 1L
        result.type shouldBe MemberLogType.TRAFFIC_ZONE_UPDATED
        result.zoneLog.zoneId shouldBe log.zoneLog.zoneId
        result.zoneLog.zoneAlias shouldBe log.zoneLog.zoneAlias
        result.zoneLog.threshold shouldBe log.zoneLog.threshold
        result.zoneLog.status shouldBe log.zoneLog.status
    }

    "관리자 Member log 'memberId' 기준 Page 조회 결과 정상 확인한다" {
        // given
        val memberId = saved.memberId
        val fromDate = LocalDate.now().minusDays(7).atStartOfDay()
        val toDate = LocalDate.now().atTime(LocalTime.MAX)

        val dto = MemberLogDTO(memberId = memberId, fromDate = fromDate, toDate = toDate)
        val pageable = PageableDTO(number = 0, size = 10)

        // when
        val result = memberZoneLogRepository.findPageByPredicate(dto, pageable)

        // then
        result.number shouldBe 0
        result.size shouldBe 10
        result.totalElements shouldBeGreaterThanOrEqual 1
        result.content.size shouldBeGreaterThanOrEqual 1
    }

})
