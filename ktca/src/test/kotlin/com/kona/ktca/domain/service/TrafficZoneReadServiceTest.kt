package com.kona.ktca.domain.service

import com.kona.common.infrastructure.enumerate.TrafficZoneStatus
import com.kona.common.infrastructure.error.ErrorCode
import com.kona.common.infrastructure.error.exception.ResourceNotFoundException
import com.kona.ktca.infrastructure.adapter.TrafficZoneFindAdapter
import com.kona.ktca.infrastructure.repository.FakeTrafficZoneRepository
import com.kona.ktca.infrastructure.repository.entity.TrafficZoneEntity
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import java.time.LocalDateTime

class TrafficZoneReadServiceTest : BehaviorSpec({

    val trafficZoneRepository = FakeTrafficZoneRepository()
    val trafficZoneFindAdapter = TrafficZoneFindAdapter(trafficZoneRepository)
    val trafficZoneReadService = TrafficZoneReadService(trafficZoneFindAdapter)

    given("트래픽 제어 Zone 단일 조회 요청되어") {

        `when`("요청 'zoneId' 기준 일치한 정보 없는 경우") {
            val result =shouldThrow<ResourceNotFoundException> { trafficZoneReadService.findTrafficZone("not-found-zoneId") }

            then("'TRAFFIC_ZONE_NOT_FOUND' 예외 발생 정상 확인한다") {
                result.errorCode shouldBe ErrorCode.TRAFFIC_ZONE_NOT_FOUND
            }
        }

        val entity = TrafficZoneEntity(
            id = "test-zone-id",
            alias = "TEST ZONE",
            threshold = 1,
            activationTime = LocalDateTime.now(),
            status = TrafficZoneStatus.ACTIVE,
        )
        trafficZoneRepository.save(entity)

        `when`("요청 'zoneId' 기준 일치한 정보 있는 경우") {
            val result = trafficZoneReadService.findTrafficZone(entity.id)

            then("조회 결과 정상 확인한다") {
                result.zoneId shouldBe entity.id
                result.zoneAlias shouldBe entity.alias
                result.threshold shouldBe entity.threshold
                result.activationTime shouldBe entity.activationTime
                result.status shouldBe entity.status
            }
        }
    }

})