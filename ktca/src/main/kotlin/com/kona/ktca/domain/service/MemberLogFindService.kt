package com.kona.ktca.domain.service

import com.kona.common.infrastructure.error.ErrorCode
import com.kona.common.infrastructure.error.exception.ResourceNotFoundException
import com.kona.ktca.domain.dto.MemberLogDTO
import com.kona.ktca.domain.dto.PageableDTO
import com.kona.ktca.domain.model.Member
import com.kona.ktca.domain.model.MemberLog
import com.kona.ktca.domain.port.inbound.MemberLogFindPort
import com.kona.ktca.domain.port.outbound.MemberRepository
import com.kona.ktca.domain.port.outbound.MemberZoneLogRepository
import org.springframework.data.domain.Page
import org.springframework.stereotype.Service

@Service
class MemberLogFindService(
    private val memberZoneLogRepository: MemberZoneLogRepository,
    private val memberRepository: MemberRepository
) : MemberLogFindPort {

    override suspend fun findPageMemberLog(
        dto: MemberLogDTO,
        pageable: PageableDTO,
    ): Page<MemberLog> {
        val member = findMember(memberId = dto.memberId)
        return memberZoneLogRepository.findPageByPredicate(dto, pageable).map { it.applyMember(member) }
    }

    private suspend fun findMember(memberId: Long): Member {
        return memberRepository.findByMemberId(memberId = memberId) ?: throw ResourceNotFoundException(ErrorCode.MEMBER_NOT_FOUND)
    }

}