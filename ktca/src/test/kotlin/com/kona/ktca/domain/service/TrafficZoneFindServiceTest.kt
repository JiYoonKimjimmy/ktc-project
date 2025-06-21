package com.kona.ktca.domain.service

import com.kona.common.infrastructure.enumerate.TrafficZoneStatus
import com.kona.common.infrastructure.error.ErrorCode
import com.kona.common.infrastructure.error.exception.ResourceNotFoundException
import com.kona.ktca.domain.model.TrafficZone
import com.kona.ktca.infrastructure.repository.FakeTrafficZoneRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import java.time.LocalDateTime

class TrafficZoneFindServiceTest : BehaviorSpec({

    val trafficZoneRepository = FakeTrafficZoneRepository()
    val trafficZoneFindService = TrafficZoneFindService(trafficZoneRepository)

    given("트래픽 제어 Zone 단일 조회 요청되어") {

        `when`("요청 'zoneId' 기준 일치한 정보 없는 경우") {
            val result =shouldThrow<ResourceNotFoundException> { trafficZoneFindService.findTrafficZone("not-found-zoneId") }

            then("'TRAFFIC_ZONE_NOT_FOUND' 예외 발생 정상 확인한다") {
                result.errorCode shouldBe ErrorCode.TRAFFIC_ZONE_NOT_FOUND
            }
        }

        val zone = TrafficZone(
            zoneId = "test-zone-id",
            zoneAlias = "TEST ZONE",
            threshold = 1,
            activationTime = LocalDateTime.now(),
            status = TrafficZoneStatus.ACTIVE,
        )
        trafficZoneRepository.save(zone)

        `when`("요청 'zoneId' 기준 일치한 정보 있는 경우") {
            val result = trafficZoneFindService.findTrafficZone(zone.zoneId)

            then("조회 결과 정상 확인한다") {
                result.zoneId shouldBe zone.zoneId
                result.zoneAlias shouldBe zone.zoneAlias
                result.threshold shouldBe zone.threshold
                result.activationTime shouldBe zone.activationTime
                result.status shouldBe zone.status
            }
        }
    }

})