package com.kona.ktca.domain.service

import com.kona.common.infrastructure.enumerate.MemberLogType
import com.kona.common.infrastructure.error.ErrorCode
import com.kona.common.infrastructure.error.exception.ResourceNotFoundException
import com.kona.ktca.domain.model.Member
import com.kona.ktca.domain.model.MemberLog
import com.kona.ktca.domain.model.TrafficZone
import com.kona.ktca.domain.port.inbound.MemberLogSavePort
import com.kona.ktca.domain.port.outbound.MemberRepository
import com.kona.ktca.domain.port.outbound.MemberZoneLogRepository
import org.springframework.stereotype.Service

@Service
class MemberLogSaveService(
    private val memberZoneLogRepository: MemberZoneLogRepository,
    private val memberRepository: MemberRepository
) : MemberLogSavePort {

    override suspend fun create(memberId: Long, type: MemberLogType, zone: TrafficZone) : MemberLog {
        val member = findMember(memberId)
        val log = MemberLog.create(member = member, type = type, zoneLog = zone)
        return memberZoneLogRepository.save(log)
    }

    private suspend fun findMember(memberId: Long): Member {
        return memberRepository.findByMemberId(memberId) ?: throw ResourceNotFoundException(ErrorCode.MEMBER_NOT_FOUND)
    }

}