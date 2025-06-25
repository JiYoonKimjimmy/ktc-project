package com.kona.ktca.infrastructure.repository

import com.kona.common.infrastructure.util.SnowflakeIdGenerator
import com.kona.ktca.domain.dto.MemberDTO
import com.kona.ktca.domain.dto.PageableDTO
import com.kona.ktca.domain.model.MemberFixture
import com.kona.ktca.infrastructure.repository.jpa.MemberJpaRepository
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.comparables.shouldBeLessThanOrEqualTo
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.springframework.boot.test.context.SpringBootTest
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@SpringBootTest
class MemberRepositoryImplTest(
    private val memberJpaRepository: MemberJpaRepository
) : StringSpec({

    val memberRepository = MemberRepositoryImpl(memberJpaRepository)

    "Member 신규 생성 결과 정상 확인한다" {
        // given
        val member = MemberFixture.giveOne()

        // when
        val result = memberRepository.save(member)

        // then
        val expected = memberRepository.findByLoginId(loginId = member.loginId)
        expected!! shouldNotBe null
        expected.memberId shouldBe result.memberId
        expected.loginId shouldBe member.loginId
        expected.password shouldBe member.password
        expected.name shouldBe member.name
        expected.email shouldBe member.email
        expected.team shouldBe member.team
        expected.role shouldBe member.role
        expected.status shouldBe member.status
        expected.lastLoginAt.truncatedTo(ChronoUnit.MILLIS) shouldBe member.lastLoginAt.truncatedTo(ChronoUnit.MILLIS)
        expected.created?.shouldBeLessThanOrEqualTo(LocalDateTime.now())
        expected.updated?.shouldBeLessThanOrEqualTo(LocalDateTime.now())
    }

    "Member 'loginId' and 'name' 기준 단건 조회 결과 정상 확인한다" {
        // given
        val member = memberRepository.save(MemberFixture.giveOne())
        val dto = MemberDTO(
            loginId = member.loginId,
            name = member.name
        )

        // when
        val result = memberRepository.findByPredicate(dto)

        // then
        result!! shouldNotBe null
        result.memberId shouldBe member.memberId
    }
    
    "Member 'team' 기준 Page 조회 결과 '2건' 정상 확인한다" {
        // given
        val team = "team-${SnowflakeIdGenerator.generate()}"
        val dto = MemberDTO(team = team)
        val pageable = PageableDTO(number = 0, size = 10)

        memberRepository.save(MemberFixture.giveOne(team = team))
        memberRepository.save(MemberFixture.giveOne(team = team))

        // when
        val result = memberRepository.findPageByPredicate(dto, pageable)

        // then
        result.totalElements shouldBe 2
        result.numberOfElements shouldBe 2
        result.content.size shouldBe 2
    }

    "Member 'team' 기준 Page 조회 마지막 결과 '1건' 정상 확인한다" {
        // given
        val team = "team-${SnowflakeIdGenerator.generate()}"
        val dto = MemberDTO(team = team)
        val pageable = PageableDTO(number = 1, size = 1)

        memberRepository.save(MemberFixture.giveOne(team = team))
        val expected = memberRepository.save(MemberFixture.giveOne(team = team))

        // when
        val result = memberRepository.findPageByPredicate(dto, pageable)

        // then
        result.totalElements shouldBe 2
        result.numberOfElements shouldBe 1
        result.content.size shouldBe 1
        result.content.first().memberId shouldBe expected.memberId
    }

    "Member 'loginId' 기준 정보 존재 여부 조회하여 정상 확인한다" {
        // given
        val entity = MemberFixture.giveOne()
        memberRepository.save(entity)

        // when
        val result = memberRepository.existsByLoginId(entity.loginId)

        // then
        result shouldBe true
    }

})
