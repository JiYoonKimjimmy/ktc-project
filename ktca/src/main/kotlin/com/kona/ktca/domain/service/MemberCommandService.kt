package com.kona.ktca.domain.service

import com.kona.common.infrastructure.error.ErrorCode
import com.kona.common.infrastructure.error.exception.InternalServiceException
import com.kona.ktca.domain.dto.MemberDTO
import com.kona.ktca.domain.model.Member
import com.kona.ktca.domain.port.inbound.MemberCommandPort
import com.kona.ktca.domain.port.outbound.MemberFindPort
import com.kona.ktca.domain.port.outbound.MemberSavePort
import org.springframework.stereotype.Service

@Service
class MemberCommandService(
    private val memberSavePort: MemberSavePort,
    private val memberFindPort: MemberFindPort
) : MemberCommandPort {

    override suspend fun create(dto: MemberDTO): Member {
        val member = Member.create(dto)
        if (memberFindPort.existsByLoginId(member.loginId)) {
            throw InternalServiceException(ErrorCode.MEMBER_LOGIN_ID_ALREADY_EXISTS)
        }
        return memberSavePort.save(Member.create(dto))
    }

}