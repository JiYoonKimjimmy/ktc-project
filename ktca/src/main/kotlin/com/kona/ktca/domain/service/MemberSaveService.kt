package com.kona.ktca.domain.service

import com.kona.common.infrastructure.error.ErrorCode
import com.kona.common.infrastructure.error.exception.InternalServiceException
import com.kona.common.infrastructure.error.exception.ResourceNotFoundException
import com.kona.ktca.domain.dto.MemberDTO
import com.kona.ktca.domain.model.Member
import com.kona.ktca.domain.port.inbound.MemberSavePort
import com.kona.ktca.domain.port.outbound.MemberRepository
import org.springframework.stereotype.Service

@Service
class MemberSaveService(
    private val memberRepository: MemberRepository
) : MemberSavePort {

    override suspend fun create(dto: MemberDTO): Member {
        return dto.validation().createMember().saved()
    }

    override suspend fun update(dto: MemberDTO): Member {
        return dto.validation().findMember().update(dto).saved()
    }

    private suspend fun MemberDTO.validation(): MemberDTO {
        if (isNeedLoginIdDuplicateCheck && memberRepository.existsByLoginId(loginId = loginId!!)) {
            throw InternalServiceException(ErrorCode.MEMBER_LOGIN_ID_ALREADY_EXISTS)
        }
        return this
    }

    private suspend fun MemberDTO.createMember(): Member {
        return Member.create(this)
    }

    private suspend fun MemberDTO.findMember(): Member {
        return memberRepository.findByMemberId(memberId = memberId!!) ?: throw ResourceNotFoundException(ErrorCode.MEMBER_NOT_FOUND)
    }

    private suspend fun Member.saved(): Member {
        return memberRepository.save(member = this)
    }

}