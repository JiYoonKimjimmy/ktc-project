package com.kona.ktca.infrastructure.repository

import com.kona.common.infrastructure.enumerate.TrafficZoneGroupStatus
import com.kona.ktca.domain.model.TrafficZoneGroupFixture
import com.kona.ktca.infrastructure.repository.jpa.TrafficZoneGroupJpaRepository
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveAtLeastSize
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.ints.shouldBeGreaterThanOrEqual
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class TrafficZoneGroupRepositoryImplTest(
    private val trafficZoneGroupJpaRepository: TrafficZoneGroupJpaRepository
) : StringSpec({

    val trafficZoneGroupRepository = TrafficZoneGroupRepositoryImpl(trafficZoneGroupJpaRepository)

    "TrafficZoneGroup 단일 등록 처리 결과 정상 확인한다" {
        // given
        val group = TrafficZoneGroupFixture.giveOne(name = "테스트 그룹", order = 1)

        // when
        val result = trafficZoneGroupRepository.save(group)

        // then
        result.groupId shouldNotBe null
        result.name shouldBe "테스트 그룹"
        result.order shouldBe 1
    }

    "TrafficZoneGroup 'max(order) + 1' 단일 등록 처리 결과 정상 확인한다" {
        // given
        val name = "테스트 그룹"

        // when
        val result = trafficZoneGroupRepository.saveNextOrder(name)

        // then
        result.groupId shouldNotBe null
        result.name shouldBe "테스트 그룹"
        result.order shouldBeGreaterThanOrEqual 1
    }

    "TrafficZoneGroup 단일 조회 결과 정상 확인한다" {
        // given
        val group = trafficZoneGroupRepository.saveNextOrder("테스트 그룹")

        // when
        val result = trafficZoneGroupRepository.findByGroupIdAndStatus(groupId = group.groupId!!, status = TrafficZoneGroupStatus.ACTIVE)

        // then
        result shouldNotBe null
    }

    "TrafficZoneGroup 전체 조회 결과 정상 확인한다" {
        // given
        trafficZoneGroupRepository.saveNextOrder("테스트 그룹")
        trafficZoneGroupRepository.saveNextOrder("테스트 그룹")

        // when
        val result = trafficZoneGroupRepository.findAllByStatus(status = TrafficZoneGroupStatus.ACTIVE)

        // then
        result shouldHaveAtLeastSize 2
    }

    "TrafficZoneGroup 단일 삭제 처리 결과 정상 확인다" {
        // given
        val group = trafficZoneGroupRepository.saveNextOrder("테스트 그룹")

        // when
        trafficZoneGroupRepository.delete(group.groupId!!)

        // then
        val result = trafficZoneGroupRepository.findAllByStatus(status = TrafficZoneGroupStatus.ACTIVE).filter { it.groupId == group.groupId }
        result shouldHaveSize 0
    }

})
