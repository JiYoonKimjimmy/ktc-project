package com.kona.ktca.domain.model

import com.kona.common.infrastructure.enumerate.MemberRole
import com.kona.common.infrastructure.enumerate.MemberStatus
import com.kona.ktca.domain.dto.MemberDTO

data class Member(
    val memberId: Long? = null,
    val loginId: String,
    val password: String,
    val name: String,
    val email: String,
    val team: String,
    val role: MemberRole,
    val status: MemberStatus
) {

    companion object {
        fun create(dto: MemberDTO): Member {
            return Member(
                loginId = dto.loginId!!,
                password = dto.password!!,
                name = dto.name!!,
                email = dto.email!!,
                team = dto.team!!,
                role = dto.role ?: MemberRole.USER,
                status = dto.status ?: MemberStatus.ACTIVE
            )
        }
    }

}