package com.kona.ktca.infrastructure.repository

import com.kona.common.infrastructure.enumerate.TrafficZoneStatus
import com.kona.common.infrastructure.util.convertPatternOf
import com.kona.ktca.domain.dto.PageableDTO
import com.kona.ktca.domain.dto.TrafficZoneDTO
import com.kona.ktca.domain.model.TrafficZone
import com.kona.ktca.infrastructure.repository.jpa.TrafficZoneJpaRepository
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.comparables.shouldBeGreaterThanOrEqualTo
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.springframework.boot.test.context.SpringBootTest
import java.time.LocalDateTime

@SpringBootTest
class TrafficZoneRepositoryImplTest(
    private val trafficZoneJpaRepository: TrafficZoneJpaRepository
) : StringSpec({

    val trafficZoneRepository = TrafficZoneRepositoryImpl(trafficZoneJpaRepository)

    lateinit var saved: TrafficZone

    beforeSpec {
        val entity = TrafficZone(
            zoneId = "test-zone-id",
            zoneAlias = "test-zone",
            threshold = 1,
            activationTime = LocalDateTime.now(),
            status = TrafficZoneStatus.ACTIVE,
        )
        saved = trafficZoneRepository.save(entity)
    }

    "TrafficZoneEntity DB 저장 처리 결과 정상 확인한다" {
        // given
        val entity = TrafficZone(
            zoneId = "zone-id",
            zoneAlias = "zone alias",
            threshold = 1,
            activationTime = LocalDateTime.now(),
            status = TrafficZoneStatus.ACTIVE,
        )
        val expectedDate = LocalDateTime.now()

        // when
        val result = trafficZoneRepository.save(entity)

        // then
        result shouldNotBe null
        result.created?.shouldBeGreaterThanOrEqualTo(expectedDate)
        result.updated?.shouldBeGreaterThanOrEqualTo(expectedDate)
    }

    "TrafficZoneEntity DB 변경 결과 정상 확인한다" {
        // given
        val entity = TrafficZone(
            zoneId = saved.zoneId,
            zoneAlias = "test-zone-updated",
            threshold = saved.threshold,
            activationTime = saved.activationTime,
            status = saved.status,
        )

        // when
        val result = trafficZoneRepository.save(entity)

        // then
        result shouldNotBe null
        result.updated!!.shouldBeGreaterThanOrEqualTo(saved.updated!!)
    }

    "TrafficZoneEntity 'id: test-zone-id' 기준 Page DB 조회 결과 정상 확인한다" {
        // given
        val dto = TrafficZoneDTO(zoneId = saved.zoneId)
        val pageable = PageableDTO(number = 0, size = 1)

        // when
        val result = trafficZoneRepository.findPage(dto, pageable)

        // then
        result.numberOfElements shouldBe 1

        val content = result.content[0]
        content!! shouldNotBe null
        content.zoneId shouldBe saved.zoneId
        content.created?.convertPatternOf() shouldBe saved.created?.convertPatternOf()
        content.updated!!.convertPatternOf() shouldBeGreaterThanOrEqualTo saved.updated!!.convertPatternOf()
    }

    "TrafficZoneEntity 'status: ACTIVE' 기준 Page DB 조회 결과 정상 확인한다" {
        // given
        val dto = TrafficZoneDTO(status = TrafficZoneStatus.ACTIVE)
        val pageable = PageableDTO(number = 0, size = 1)

        // when
        val result = trafficZoneRepository.findPage(dto, pageable)

        // then
        result.numberOfElements shouldBe 1
    }

})