package com.kona.ktca.v1.infrastructure.repository

import com.kona.common.infrastructure.enumerate.TrafficZoneStatus
import com.kona.ktca.v1.domain.dto.TrafficZoneDTO
import com.kona.ktca.v1.infrastructure.repository.entity.TrafficZoneEntity
import com.kona.ktca.v1.infrastructure.repository.jpa.TrafficZoneJpaRepository
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.PageRequest
import java.time.LocalDateTime

@SpringBootTest
class TrafficZoneRepositoryImplTest(
    private val trafficZoneJpaRepository: TrafficZoneJpaRepository
) : StringSpec({

    val trafficZoneRepository = TrafficZoneRepositoryImpl(trafficZoneJpaRepository)

    beforeSpec {
        val entity = TrafficZoneEntity(
            id = "test-zone-id",
            alias = "test-zone",
            threshold = 1,
            activationTime = LocalDateTime.now(),
            status = TrafficZoneStatus.ACTIVE,
        )
        trafficZoneRepository.save(entity)
    }

    "TrafficZoneEntity DB 저장 처리 결과 정상 확인한다" {
        // given
        val entity = TrafficZoneEntity(
            id = "zone-id",
            alias = "zone alias",
            threshold = 1,
            activationTime = LocalDateTime.now(),
            status = TrafficZoneStatus.ACTIVE,
        )

        // when
        val result = trafficZoneRepository.save(entity)

        // then
        result shouldNotBe null
    }

    "TrafficZoneEntity 'status: ACTIVE' 기준 Page DB 조회 결과 정상 확인한다" {
        // given
        val where = TrafficZoneDTO(status = TrafficZoneStatus.ACTIVE).toPredicatable()
        val pageable = PageRequest.of(0,1)

        // when
        val result = trafficZoneRepository.findPage(where, pageable)

        // then
        result.numberOfElements shouldBe 1
    }

})