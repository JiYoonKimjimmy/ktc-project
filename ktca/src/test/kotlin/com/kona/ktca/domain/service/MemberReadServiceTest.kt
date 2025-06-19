package com.kona.ktca.domain.service

import com.kona.common.infrastructure.error.ErrorCode
import com.kona.common.infrastructure.error.exception.ResourceNotFoundException
import com.kona.ktca.domain.dto.MemberDTO
import com.kona.ktca.infrastructure.adapter.MemberFindAdapter
import com.kona.ktca.infrastructure.repository.FakeMemberRepository
import com.kona.ktca.infrastructure.repository.entity.MemberEntityFixture
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

class MemberReadServiceTest : BehaviorSpec({

    val memberRepository = FakeMemberRepository()
    val memberFindPort = MemberFindAdapter(memberRepository)
    val memberReadService = MemberReadService(memberFindPort)

    val memberEntityFixture = MemberEntityFixture()

    given("'loginId' 기준 관리자 정보 단일 조회 요청하여") {
        val notExistLoginIdDTO = MemberDTO(loginId = "notExistLoginId")

        `when`("일치한 정보 없는 경우") {
            val exception = shouldThrow<ResourceNotFoundException> { memberReadService.findMember(notExistLoginIdDTO) }

            then("'MEMBER_NOT_FOUND' 예외 발생 정상 확인한다") {
                exception.errorCode shouldBe ErrorCode.MEMBER_NOT_FOUND
            }
        }

        val saved = memberRepository.save(memberEntityFixture.giveOne("testLoginId"))
        val dto = MemberDTO(loginId = saved.loginId)

        `when`("일치한 정보 있는 경우") {
            val result = memberReadService.findMember(dto)

            then("조회 결과 정상 확인한다") {
                result.memberId shouldBe saved.id
                result.loginId shouldBe saved.loginId
                result.password shouldBe saved.password
                result.name shouldBe saved.name
                result.email shouldBe saved.email
                result.team shouldBe saved.team
                result.role shouldBe saved.role
                result.status shouldBe saved.status
                result.lastLoginAt shouldBe saved.lastLoginAt
            }
        }
    }

})
