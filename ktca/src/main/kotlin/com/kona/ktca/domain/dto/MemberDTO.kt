package com.kona.ktca.domain.dto

import com.kona.common.infrastructure.enumerate.MemberRole
import com.kona.common.infrastructure.enumerate.MemberStatus
import java.time.LocalDateTime

data class MemberDTO(
    val memberId: Long? = null,
    val loginId: String? = null,
    val password: String? = null,
    val name: String? = null,
    val email: String? = null,
    val team: String? = null,
    val role: MemberRole? = null,
    val status: MemberStatus? = null,
    val lastLoginAt: LocalDateTime? = null,
)