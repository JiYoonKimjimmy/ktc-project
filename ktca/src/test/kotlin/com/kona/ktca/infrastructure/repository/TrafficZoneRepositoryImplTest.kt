package com.kona.ktca.infrastructure.repository

import com.kona.common.infrastructure.enumerate.TrafficZoneStatus
import com.kona.common.infrastructure.util.convertPatternOf
import com.kona.ktca.domain.dto.PageableDTO
import com.kona.ktca.domain.dto.TrafficZoneDTO
import com.kona.ktca.domain.model.TrafficZone
import com.kona.ktca.domain.model.TrafficZoneFixture
import com.kona.ktca.domain.model.TrafficZoneGroupFixture
import com.kona.ktca.infrastructure.repository.jpa.TrafficZoneGroupJpaRepository
import com.kona.ktca.infrastructure.repository.jpa.TrafficZoneJpaRepository
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.comparables.shouldBeGreaterThanOrEqualTo
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.springframework.boot.test.context.SpringBootTest
import java.time.LocalDateTime

@SpringBootTest
class TrafficZoneRepositoryImplTest(
    private val trafficZoneJpaRepository: TrafficZoneJpaRepository,
    private val trafficZoneGroupJpaRepository: TrafficZoneGroupJpaRepository
) : StringSpec({

    val trafficZoneRepository = TrafficZoneRepositoryImpl(trafficZoneJpaRepository)
    val trafficZoneGroupRepository = TrafficZoneGroupRepositoryImpl(trafficZoneGroupJpaRepository)

    lateinit var saved: TrafficZone

    beforeSpec {
        val group = trafficZoneGroupRepository.saveNextOrder(TrafficZoneGroupFixture.giveOne())
        saved = trafficZoneRepository.save(TrafficZoneFixture.giveOne(group = group))
    }

    "TrafficZoneEntity DB 저장 처리 결과 정상 확인한다" {
        // given
        val group = trafficZoneGroupRepository.saveNextOrder(TrafficZoneGroupFixture.giveOne())
        val zone = TrafficZoneFixture.giveOne(group = group)
        val expectedDate = LocalDateTime.now()

        // when
        val result = trafficZoneRepository.save(zone)

        // then
        result shouldNotBe null
        result.zoneId shouldBe zone.zoneId
        result.created?.shouldBeGreaterThanOrEqualTo(expectedDate)
        result.updated?.shouldBeGreaterThanOrEqualTo(expectedDate)
    }

    "TrafficZoneEntity 'zoneAlias' DB 변경 결과 정상 확인한다" {
        // given
        val zone = TrafficZone(
            zoneId = saved.zoneId,
            zoneAlias = "test-zone-updated",
            threshold = saved.threshold,
            activationTime = saved.activationTime,
            status = saved.status,
            group = saved.group
        )
        val expectedDate = LocalDateTime.now()

        // when
        val result = trafficZoneRepository.save(zone)

        // then
        result shouldNotBe null
        result.zoneAlias shouldBe zone.zoneAlias
        result.updated!!.shouldBeGreaterThanOrEqualTo(expectedDate)
    }

    "TrafficZoneEntity 'group' DB 변경 결과 정상 확인한다" {
        // given
        val newGroup = trafficZoneGroupRepository.saveNextOrder(TrafficZoneGroupFixture.giveOne())
        val zone = TrafficZone(
            zoneId = saved.zoneId,
            zoneAlias = saved.zoneAlias,
            threshold = saved.threshold,
            activationTime = saved.activationTime,
            status = saved.status,
            group = newGroup
        )
        val expectedDate = LocalDateTime.now()

        // when
        val result = trafficZoneRepository.save(zone)

        // then
        result shouldNotBe null
        result.groupId shouldBe zone.groupId
        result.groupId shouldNotBe saved.groupId
        result.updated!!.shouldBeGreaterThanOrEqualTo(expectedDate)
    }

    "TrafficZoneEntity 'id' 기준 DB 조회 결과 정상 확인한다" {
        // given
        val dto = TrafficZoneDTO(zoneId = saved.zoneId)

        // when
        val result = trafficZoneRepository.findByPredicate(dto)

        // then
        result!! shouldNotBe null
        result.zoneId shouldBe saved.zoneId
    }

    "TrafficZoneEntity 'groupId' 기준 DB 조회 결과 정상 확인한다" {
        // given
        val group = trafficZoneGroupRepository.saveNextOrder(TrafficZoneGroupFixture.giveOne())
        saved = trafficZoneRepository.save(TrafficZoneFixture.giveOne(group = group))
        val dto = TrafficZoneDTO(groupId = saved.groupId)

        // when
        val result = trafficZoneRepository.findByPredicate(dto)

        // then
        result!! shouldNotBe null
        result.zoneId shouldBe saved.zoneId
        result.groupId shouldBe saved.groupId
    }

    "TrafficZoneEntity 'id' 기준 Page DB 조회 결과 정상 확인한다" {
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
        content.groupId shouldNotBe null
        content.created?.convertPatternOf() shouldBe saved.created?.convertPatternOf()
        content.updated!!.convertPatternOf() shouldBeGreaterThanOrEqualTo saved.updated!!.convertPatternOf()
    }

    "TrafficZoneEntity 'status' 기준 Page DB 조회 결과 정상 확인한다" {
        // given
        val dto = TrafficZoneDTO(status = TrafficZoneStatus.ACTIVE)
        val pageable = PageableDTO(number = 0, size = 1)

        // when
        val result = trafficZoneRepository.findPage(dto, pageable)

        // then
        result.numberOfElements shouldBe 1
    }

})