package com.kona.ktca.infrastructure.repository

import com.kona.ktca.domain.dto.MemberDTO
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
    val memberFixture = MemberFixture()

    "Member entity 신규 생성하여 정상 확인한다" {
        // given
        val entity = memberFixture.giveOne()

        // when
        val result = memberRepository.save(entity)

        // then
        val expected = memberRepository.findByLoginId(loginId = entity.loginId)
        expected!! shouldNotBe null
        expected.memberId shouldBe result.memberId
        expected.loginId shouldBe entity.loginId
        expected.password shouldBe entity.password
        expected.name shouldBe entity.name
        expected.email shouldBe entity.email
        expected.team shouldBe entity.team
        expected.role shouldBe entity.role
        expected.status shouldBe entity.status
        expected.lastLoginAt.truncatedTo(ChronoUnit.MILLIS) shouldBe entity.lastLoginAt.truncatedTo(ChronoUnit.MILLIS)
        expected.created?.shouldBeLessThanOrEqualTo(LocalDateTime.now())
        expected.updated?.shouldBeLessThanOrEqualTo(LocalDateTime.now())
    }

    "Member entity 'loginId' 기준 정보 존재 여부 조회하여 정상 확인한다" {
        // given
        val entity = memberFixture.giveOne()
        memberRepository.save(entity)

        // when
        val result = memberRepository.existsByLoginId(entity.loginId)

        // then
        result shouldBe true
    }

    "Member entity 'loginId' and 'name' 기준 조회하여 정상 확인한다" {
        // given
        val entity = memberRepository.save(memberFixture.giveOne())
        val dto = MemberDTO(
            loginId = entity.loginId,
            name = entity.name
        )

        // when
        val result = memberRepository.findByPredicate(dto)

        // then
        result!! shouldNotBe null
        result.memberId shouldBe entity.memberId
    }

})
