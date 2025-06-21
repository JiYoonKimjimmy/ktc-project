package com.kona.ktca.domain.service

import com.kona.common.infrastructure.error.ErrorCode
import com.kona.common.infrastructure.error.exception.ResourceNotFoundException
import com.kona.ktca.domain.dto.MemberDTO
import com.kona.ktca.domain.dto.PageableDTO
import com.kona.ktca.domain.model.Member
import com.kona.ktca.domain.port.inbound.MemberFindPort
import com.kona.ktca.domain.port.outbound.MemberRepository
import org.springframework.data.domain.Page
import org.springframework.stereotype.Service

@Service
class MemberFindService(
    private val memberRepository: MemberRepository
) : MemberFindPort {

    override suspend fun findMember(dto: MemberDTO): Member {
        return memberRepository.findByPredicate(dto)
            ?: throw ResourceNotFoundException(ErrorCode.MEMBER_NOT_FOUND)
    }

    override suspend fun findPageMember(dto: MemberDTO, pageable: PageableDTO): Page<Member> {
        return memberRepository.findPageByPredicate(dto, pageable)
    }

}