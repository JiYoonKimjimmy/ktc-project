package com.kona.ktca.infrastructure.adapter

import com.kona.ktca.domain.port.outbound.MemberFindPort
import com.kona.ktca.infrastructure.repository.MemberRepository
import org.springframework.stereotype.Component
import kotlin.math.log

@Component
class MemberFindAdapter(
    private val memberRepository: MemberRepository
) : MemberFindPort {

    override suspend fun existsByLoginId(loginId: String): Boolean {
        return memberRepository.existsByLoginId(loginId)
    }

}