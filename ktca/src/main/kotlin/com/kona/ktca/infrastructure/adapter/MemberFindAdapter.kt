package com.kona.ktca.infrastructure.adapter

import com.kona.ktca.domain.dto.MemberDTO
import com.kona.ktca.domain.model.Member
import com.kona.ktca.domain.port.outbound.MemberFindPort
import com.kona.ktca.infrastructure.repository.MemberRepository
import org.springframework.stereotype.Component

@Component
class MemberFindAdapter(
    private val memberRepository: MemberRepository
) : MemberFindPort {

    override suspend fun findByPredicate(dto: MemberDTO): Member? {
        return memberRepository.findByPredicate(dto)?.toDomain()
    }

    override suspend fun existsByLoginId(loginId: String): Boolean {
        return memberRepository.existsByLoginId(loginId)
    }

}