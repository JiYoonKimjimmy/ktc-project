package com.kona.ktca.domain.service

import com.kona.common.infrastructure.error.ErrorCode
import com.kona.common.infrastructure.error.exception.ResourceNotFoundException
import com.kona.common.infrastructure.util.SnowflakeIdGenerator
import com.kona.ktca.domain.dto.MemberDTO
import com.kona.ktca.domain.dto.PageableDTO
import com.kona.ktca.domain.model.MemberFixture
import com.kona.ktca.infrastructure.repository.FakeMemberRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.longs.shouldBeGreaterThanOrEqual
import io.kotest.matchers.shouldBe

class MemberFindServiceTest : BehaviorSpec({

    val memberRepository = FakeMemberRepository()
    val memberFindService = MemberFindService(memberRepository)

    val memberFixture = MemberFixture()

    given("'loginId' 기준 관리자 정보 단일 조회 요청하여") {
        val notExistLoginIdDTO = MemberDTO(loginId = "notExistLoginId")

        `when`("일치한 정보 없는 경우") {
            val exception = shouldThrow<ResourceNotFoundException> { memberFindService.findMember(notExistLoginIdDTO) }

            then("'MEMBER_NOT_FOUND' 예외 발생 정상 확인한다") {
                exception.errorCode shouldBe ErrorCode.MEMBER_NOT_FOUND
            }
        }

        val saved = memberRepository.save(memberFixture.giveOne("testLoginId"))
        val dto = MemberDTO(loginId = saved.loginId)

        `when`("일치한 정보 있는 경우") {
            val result = memberFindService.findMember(dto)

            then("조회 결과 정상 확인한다") {
                result.memberId shouldBe saved.memberId
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

    given("'team' 기준 관리자 정보 목록 조회 요청하여") {
        val notExistTeamDTO = MemberDTO(team = "not-exist-team")
        val pageable = PageableDTO(number = 0, size = 10)

        `when`("일치한 정보 없는 경우") {
            val result = memberFindService.findPageMember(notExistTeamDTO, pageable)

            then("조회 결과 '0건' 정상 확인한다") {
                result.totalElements shouldBeGreaterThanOrEqual 0
                result.numberOfElements shouldBe 0
                result.content.size shouldBe 0
            }
        }

        val team = "team-${SnowflakeIdGenerator.generate()}"
        val dto = MemberDTO(team = team)
        memberRepository.save(memberFixture.giveOne(team = team))
        memberRepository.save(memberFixture.giveOne(team = team))

        `when`("일치한 정보 총 '2건' 있는 경우") {
            val result = memberFindService.findPageMember(dto, pageable)

            then("조회 결과 '2건' 정상 확인한다") {
                result.totalElements shouldBeGreaterThanOrEqual 2
                result.numberOfElements shouldBe 2
                result.content.size shouldBe 2
            }
        }

        val lastPageable = PageableDTO(number = 1, size = 1)

        `when`("일치한 정보 중 마지막 Page number 조회 요청인 있는 경우") {
            val result = memberFindService.findPageMember(dto, lastPageable)

            then("조회 결과 '1건' 정상 확인한다") {
                result.totalElements shouldBeGreaterThanOrEqual 2
                result.numberOfElements shouldBe 1
                result.content.size shouldBe 1
            }
        }
    }

})
