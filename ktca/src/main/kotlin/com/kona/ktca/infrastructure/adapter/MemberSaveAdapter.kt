package com.kona.ktca.infrastructure.adapter

import com.kona.ktca.domain.model.Member
import com.kona.ktca.domain.port.outbound.MemberSavePort
import com.kona.ktca.infrastructure.repository.MemberRepository
import com.kona.ktca.infrastructure.repository.entity.MemberEntity
import org.springframework.stereotype.Component

@Component
class MemberSaveAdapter(
    private val memberRepository: MemberRepository
) : MemberSavePort {

    override suspend fun save(member: Member): Member {
        return MemberEntity.of(member)
            .let { memberRepository.save(it) }
            .toDomain()
    }

}