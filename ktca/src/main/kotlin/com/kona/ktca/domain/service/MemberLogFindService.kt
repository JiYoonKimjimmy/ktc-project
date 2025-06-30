package com.kona.ktca.domain.service

import com.kona.ktca.domain.dto.MemberLogDTO
import com.kona.ktca.domain.dto.PageableDTO
import com.kona.ktca.domain.model.MemberLog
import com.kona.ktca.domain.port.inbound.MemberLogFindPort
import com.kona.ktca.domain.port.outbound.MemberZoneLogRepository
import org.springframework.data.domain.Page
import org.springframework.stereotype.Service

@Service
class MemberLogFindService(
    private val memberZoneLogRepository: MemberZoneLogRepository
) : MemberLogFindPort {

    override suspend fun findPageMemberLog(dto: MemberLogDTO, pageable: PageableDTO): Page<MemberLog> {
        return memberZoneLogRepository.findPageByPredicate(dto, pageable)
    }

}