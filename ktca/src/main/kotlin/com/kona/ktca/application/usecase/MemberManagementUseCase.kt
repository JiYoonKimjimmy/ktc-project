package com.kona.ktca.application.usecase

import com.kona.ktca.domain.dto.MemberDTO
import com.kona.ktca.domain.dto.MemberLogDTO
import com.kona.ktca.domain.dto.PageableDTO
import com.kona.ktca.domain.model.Member
import com.kona.ktca.domain.model.MemberLog
import com.kona.ktca.domain.port.inbound.MemberFindPort
import com.kona.ktca.domain.port.inbound.MemberLogFindPort
import com.kona.ktca.domain.port.inbound.MemberSavePort
import org.springframework.data.domain.Page
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Transactional(readOnly = true)
@Component
class MemberManagementUseCase(
    private val memberSavePort: MemberSavePort,
    private val memberFindPort: MemberFindPort,
    private val memberLogFindPort: MemberLogFindPort,
) {

    @Transactional
    suspend fun createMember(dto: MemberDTO): Long {
        return memberSavePort.create(dto).memberId!!
    }

    suspend fun findMember(dto: MemberDTO): Member {
        return memberFindPort.findMember(dto)
    }

    suspend fun findPageMember(dto: MemberDTO, pageable: PageableDTO): Page<Member> {
        return memberFindPort.findPageMember(dto, pageable)
    }

    @Transactional
    suspend fun updateMember(dto: MemberDTO): Long {
        return memberSavePort.update(dto).memberId!!
    }

    suspend fun findPageMemberLog(dto: MemberLogDTO, pageable: PageableDTO): Page<MemberLog> {
        return memberLogFindPort.findPageMemberLog(dto, pageable)
    }

}