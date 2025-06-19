package com.kona.ktca.domain.port.outbound

import com.kona.ktca.domain.dto.MemberDTO
import com.kona.ktca.domain.dto.PageableDTO
import com.kona.ktca.domain.model.Member
import org.springframework.data.domain.Page

interface MemberRepository {

    suspend fun save(member: Member): Member

    suspend fun findByMemberId(memberId: Long): Member?

    suspend fun findByLoginId(loginId: String): Member?

    suspend fun findByPredicate(dto: MemberDTO): Member?

    suspend fun findPageByPredicate(dto: MemberDTO, pageable: PageableDTO): Page<Member>

    suspend fun existsByLoginId(loginId: String): Boolean

}