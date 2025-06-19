package com.kona.ktca.domain.service

import com.kona.common.infrastructure.error.ErrorCode
import com.kona.common.infrastructure.error.exception.InternalServiceException
import com.kona.common.infrastructure.error.exception.ResourceNotFoundException
import com.kona.common.infrastructure.util.SnowflakeIdGenerator
import com.kona.ktca.domain.dto.MemberDTO
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
    
    given("관리자 정보 생성 요청되어") {
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

    given("관리자 정보 변경 요청되어") {
        val notExistsMember = memberDTOFixture.giveOne(memberId = 1234567890)

        `when`("요청 'memberId' 기준 동일한 정보 없는 경우") {
            val exception = shouldThrow<ResourceNotFoundException> { memberSaveService.update(notExistsMember) }

            then("'MEMBER_NOT_FOUND' 예외 발생 정상 확인한다") {
                exception.errorCode shouldBe ErrorCode.MEMBER_NOT_FOUND
            }
        }

        val newMember = memberSaveService.create(memberDTOFixture.giveOne())
        val duplicateLoginIdMember = MemberDTO(memberId = newMember.memberId, loginId = saved.loginId)

        `when`("요청 'loginId' 기준 동일한 정보 있는 경우") {
            val exception = shouldThrow<InternalServiceException> { memberSaveService.update(duplicateLoginIdMember) }

            then("'MEMBER_LOGIN_ID_ALREADY_EXISTS' 예외 발생 정상 확인한다") {
                exception.errorCode shouldBe ErrorCode.MEMBER_LOGIN_ID_ALREADY_EXISTS
            }
        }

        val updateLoginId = "new-loginId-${SnowflakeIdGenerator.generate()}"
        val updateLoginIdMember = MemberDTO(memberId = newMember.memberId, loginId = updateLoginId)

        `when`("중복 없는 'loginId' 변경 요청인 경우") {
            val result = memberSaveService.update(updateLoginIdMember)

            then("관리자 'loginId' 정보 변경 처리 결과 정상 확인한다") {
                result.memberId shouldBe newMember.memberId
                result.loginId shouldBe updateLoginId
            }
        }

        val updatePassword = SnowflakeIdGenerator.generate()
        val updatePasswordMember = MemberDTO(memberId = newMember.memberId, password = updatePassword)

        `when`("'password' 변경 요청인 경우") {
            val result = memberSaveService.update(updatePasswordMember)

            then("관리자 'password' 정보 변경 처리 결과 정상 확인한다") {
                result.memberId shouldBe newMember.memberId
                result.password shouldBe updatePassword
            }
        }
    }

})
