package com.kona.ktca.domain.port.outbound

import com.kona.ktca.domain.dto.MemberDTO
import com.kona.ktca.domain.model.Member

interface MemberFindPort {

    suspend fun findByPredicate(dto: MemberDTO): Member?

    suspend fun existsByLoginId(loginId: String): Boolean

}