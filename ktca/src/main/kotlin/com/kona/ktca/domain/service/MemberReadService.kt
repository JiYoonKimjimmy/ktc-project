package com.kona.ktca.domain.service

import com.kona.common.infrastructure.error.ErrorCode
import com.kona.common.infrastructure.error.exception.ResourceNotFoundException
import com.kona.ktca.domain.dto.MemberDTO
import com.kona.ktca.domain.model.Member
import com.kona.ktca.domain.port.inbound.MemberReadPort
import com.kona.ktca.domain.port.outbound.MemberFindPort
import org.springframework.stereotype.Service

@Service
class MemberReadService(
    private val memberFindPort: MemberFindPort
) : MemberReadPort {

    override suspend fun findMember(dto: MemberDTO): Member {
        return memberFindPort.findByPredicate(dto)
            ?: throw ResourceNotFoundException(ErrorCode.MEMBER_NOT_FOUND)
    }

}