package com.kona.ktca.domain.port.inbound

import com.kona.ktca.domain.dto.MemberDTO
import com.kona.ktca.domain.model.Member

interface MemberSavePort {

    suspend fun create(dto: MemberDTO): Member

}