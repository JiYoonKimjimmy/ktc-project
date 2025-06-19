package com.kona.ktca.domain.service

import com.kona.common.infrastructure.error.ErrorCode
import com.kona.common.infrastructure.error.exception.InternalServiceException
import com.kona.ktca.domain.dto.MemberDTOFixture
import com.kona.ktca.domain.model.Member
import com.kona.ktca.infrastructure.repository.FakeMemberRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.comparables.shouldBeLessThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import java.time.LocalDateTime

class MemberSaveServiceTest : BehaviorSpec({

    val memberRepository = FakeMemberRepository()
    val memberSaveService = MemberSaveService(memberRepository)

    val memberDTOFixture = MemberDTOFixture()
    lateinit var saved: Member

    beforeSpec {
        val dto = memberDTOFixture.giveOne(loginId = "test")
        saved = memberSaveService.create(dto)
    }
    
    given("관리자 정보 저장 요청되어") {
        val duplicateLoginIdMember = memberDTOFixture.giveOne(loginId = saved.loginId)

        `when`("요청 'loginId' 기준 동일한 정보 있는 경우") {
            val exception = shouldThrow<InternalServiceException> { memberSaveService.create(duplicateLoginIdMember) }
            
            then("'MEMBER_LOGIN_ID_ALREADY_EXISTS' 예외 발생 정상 확인한다") {
                exception.errorCode shouldBe ErrorCode.MEMBER_LOGIN_ID_ALREADY_EXISTS
            }
        }

        val newLoginIdMember = memberDTOFixture.giveOne(loginId = "new-loginId")

        `when`("신규 정상 정보인 경우") {
            val result = memberSaveService.create(newLoginIdMember)

            then("관리자 정보 신규 등록 정상 확인한다") {
                result.memberId shouldNotBe null
                result.loginId shouldBe newLoginIdMember.loginId
                result.password shouldBe newLoginIdMember.password
                result.name shouldBe newLoginIdMember.name
                result.email shouldBe newLoginIdMember.email
                result.team shouldBe newLoginIdMember.team
                result.role shouldBe newLoginIdMember.role
                result.status shouldBe newLoginIdMember.status
                result.created!! shouldBeLessThan LocalDateTime.now()
                result.updated!! shouldBeLessThan LocalDateTime.now()
            }
        }
    }

})
