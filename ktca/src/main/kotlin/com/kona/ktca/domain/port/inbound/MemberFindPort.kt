package com.kona.ktca.domain.port.inbound

import com.kona.ktca.domain.dto.MemberDTO
import com.kona.ktca.domain.model.Member

interface MemberFindPort {

    suspend fun findMember(dto: MemberDTO): Member

}