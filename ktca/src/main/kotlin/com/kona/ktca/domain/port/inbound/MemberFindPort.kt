package com.kona.ktca.domain.port.inbound

import com.kona.ktca.domain.dto.MemberDTO
import com.kona.ktca.domain.dto.PageableDTO
import com.kona.ktca.domain.model.Member
import org.springframework.data.domain.Page

interface MemberFindPort {

    suspend fun findMember(dto: MemberDTO): Member

    suspend fun findMembers(dto: MemberDTO, pageable: PageableDTO): Page<Member>

}