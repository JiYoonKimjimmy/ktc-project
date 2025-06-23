package com.kona.ktca.domain.service

import com.kona.common.infrastructure.enumerate.TrafficZoneGroupStatus
import com.kona.common.infrastructure.error.ErrorCode
import com.kona.common.infrastructure.error.exception.ResourceNotFoundException
import com.kona.ktca.domain.dto.TrafficZoneGroupDTO
import com.kona.ktca.infrastructure.repository.FakeTrafficZoneGroupRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.ints.shouldBeGreaterThanOrEqual
import io.kotest.matchers.longs.shouldBeGreaterThan
import io.kotest.matchers.shouldBe

class TrafficZoneGroupSaveServiceTest : BehaviorSpec({

    val trafficZoneGroupRepository = FakeTrafficZoneGroupRepository()
    val trafficZoneGroupSaveService = TrafficZoneGroupSaveService(trafficZoneGroupRepository)

    given("트래픽 Zone 그룹 정보 신규 생성 요청되어") {
        val name = "트래픽 Zone 그룹"

        `when`("저장 처리 성공인 경우") {
            val result = trafficZoneGroupSaveService.create(name)

            then("신규 정보 생성 처리 결과 정상 확인한다") {
                result.groupId!! shouldBeGreaterThan 0
                result.name shouldBe name
                result.order shouldBeGreaterThanOrEqual 1
            }
        }
    }

    given("트래픽 Zone 그룹 정보 수정 요청되어") {
        val notExistGroupId = 999L
        val notExistGroupDTO = TrafficZoneGroupDTO(groupId = notExistGroupId, order = 2)

        `when`("요청 'groupId' 기준 일치한 정보 없는 경우") {
            val exception = shouldThrow<ResourceNotFoundException> { trafficZoneGroupSaveService.update(notExistGroupDTO) }

            then("'TRAFFIC_ZONE_GROUP_NOT_FOUND' 예외 발생 정상 확인한다") {
                exception.errorCode shouldBe ErrorCode.TRAFFIC_ZONE_GROUP_NOT_FOUND
            }
        }

        val group = trafficZoneGroupSaveService.create(name = "트래픽 Zone 그룹")
        val updateOrderDTO = TrafficZoneGroupDTO(groupId = group.groupId, order = 2)

        `when`("요청 'groupId' 기준 일치한 정보 'order: 2' 정보 변경인 경우") {
            val result = trafficZoneGroupSaveService.update(updateOrderDTO)

            then("정보 변경 처리 결과 정상 확인한다") {
                result.groupId shouldBe group.groupId
                result.name shouldBe group.name
                result.order shouldBe updateOrderDTO.order
            }
        }

        val updateStatusDTO = TrafficZoneGroupDTO(groupId = group.groupId, status = TrafficZoneGroupStatus.DELETED)

        `when`("요청 'groupId' 기준 일치한 정보 'status: DELETED' 정보 변경인 경우") {
            val result = trafficZoneGroupSaveService.update(updateStatusDTO)

            then("정보 변경 처리 결과 정상 확인한다") {
                result.groupId shouldBe group.groupId
                result.name shouldBe group.name
                result.status shouldBe updateStatusDTO.status
            }
        }
    }

})
