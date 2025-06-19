package com.kona.ktca.domain.service

import com.kona.common.infrastructure.error.ErrorCode
import com.kona.common.infrastructure.error.exception.InternalServiceException
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
        val member = Member.create(dto)
        if (memberRepository.existsByLoginId(member.loginId)) {
            throw InternalServiceException(ErrorCode.MEMBER_LOGIN_ID_ALREADY_EXISTS)
        }
        return memberRepository.save(member)
    }

}