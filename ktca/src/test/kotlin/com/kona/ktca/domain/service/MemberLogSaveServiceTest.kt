package com.kona.ktca.domain.service

import com.kona.common.infrastructure.enumerate.MemberLogType
import com.kona.common.infrastructure.error.ErrorCode
import com.kona.common.infrastructure.error.exception.ResourceNotFoundException
import com.kona.ktca.domain.model.MemberFixture
import com.kona.ktca.domain.model.TrafficZoneFixture
import com.kona.ktca.infrastructure.repository.FakeMemberRepository
import com.kona.ktca.infrastructure.repository.FakeMemberZoneLogRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class MemberLogSaveServiceTest : BehaviorSpec({

    val memberZoneLogRepository = FakeMemberZoneLogRepository()
    val memberRepository = FakeMemberRepository()
    val memberLogSaveService = MemberLogSaveService(memberZoneLogRepository, memberRepository)

    given("관리자 Zone 로그 정보 단일 생성 요청되어") {
        val notExistsMemberId = 999L
        val type = MemberLogType.TRAFFIC_ZONE_CREATED
        val zone = TrafficZoneFixture.giveOne()

        `when`("요청 'memberId' 기준 관리자 정보 없는 경우") {
            val exception = shouldThrow<ResourceNotFoundException> { memberLogSaveService.create(memberId = notExistsMemberId, type = type, zone = zone) }

            then("'MEMBER_NOT_FOUND' 예외 발생 정상 확인한다") {
                exception.errorCode shouldBe ErrorCode.MEMBER_NOT_FOUND
            }
        }

        val member = memberRepository.save(MemberFixture.giveOne())
        val memberId = member.memberId!!

        `when`("Zone 로그 저장 처리 성공인 경우") {
            val result = memberLogSaveService.create(memberId = memberId, type = type, zone = zone)

            then("처리 결과 정상 확인한다") {
                result.logId shouldNotBe null
                result.member.memberId shouldBe member.memberId
                result.type shouldBe type
                result.zoneLog.zoneId shouldBe zone.zoneId
            }
        }
    }

})
