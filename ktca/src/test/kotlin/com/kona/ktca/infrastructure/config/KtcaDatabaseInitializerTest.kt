package com.kona.ktca.infrastructure.config

import com.kona.ktca.infrastructure.repository.jpa.MemberJpaRepository
import com.kona.ktca.infrastructure.repository.jpa.TrafficZoneGroupJpaRepository
import com.kona.ktca.infrastructure.repository.jpa.TrafficZoneJpaRepository
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class KtcaDatabaseInitializerTest(
    private val memberJpaRepository: MemberJpaRepository,
    private val trafficZoneJpaRepository: TrafficZoneJpaRepository,
    private val trafficZoneGroupJpaRepository: TrafficZoneGroupJpaRepository
) : StringSpec({

    "Flyway DB 테이블 생성 & default 데이터 적재 정상 확인한다" {
        // given
        // when
        val members = memberJpaRepository.findAll()
        val trafficZones = trafficZoneJpaRepository.findAll()
        val trafficZoneGroups = trafficZoneGroupJpaRepository.findAll()

        // then
        members shouldHaveSize 1
        trafficZones shouldHaveSize 3
        trafficZoneGroups shouldHaveSize 1
    }

})
