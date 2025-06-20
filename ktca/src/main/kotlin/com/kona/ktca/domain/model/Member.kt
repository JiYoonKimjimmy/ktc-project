package com.kona.ktca.domain.model

import com.kona.common.infrastructure.enumerate.MemberRole
import com.kona.common.infrastructure.enumerate.MemberStatus
import com.kona.ktca.domain.dto.MemberDTO
import java.time.LocalDateTime

data class Member(
    val memberId: Long? = null,
    val loginId: String,
    val password: String,
    val name: String,
    val email: String,
    val team: String,
    val role: MemberRole,
    val status: MemberStatus,
    val lastLoginAt: LocalDateTime,
    val created: LocalDateTime? = null,
    val updated: LocalDateTime? = null
) {

    companion object {
        fun create(dto: MemberDTO): Member {
            return Member(
                loginId = dto.loginId!!,
                password = dto.password!!,
                name = dto.name!!,
                email = dto.email!!,
                team = dto.team!!,
                role = dto.role ?: MemberRole.VIEWER,
                status = dto.status ?: MemberStatus.ACTIVE,
                lastLoginAt = dto.lastLoginAt ?: LocalDateTime.now()
            )
        }
    }

    fun update(dto: MemberDTO): Member {
        return copy(
            loginId = dto.loginId ?: loginId,
            password = dto.password ?: password,
            name = dto.name ?: name,
            email = dto.email ?: email,
            team = dto.team ?: team,
            role = dto.role ?: role,
            status = dto.status ?: status,
            lastLoginAt = dto.lastLoginAt ?: lastLoginAt
        )
    }

}