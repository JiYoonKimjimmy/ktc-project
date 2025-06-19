package com.kona.common.infrastructure.enumerate

import com.kona.common.infrastructure.enumerate.MemberAuthority.*

enum class MemberRole(
    private val note: String,
    private val authorities: List<MemberAuthority>
) {

    SUPER_ADMIN(
        note = "슈퍼 관리자",
        authorities = listOf(
            MEMBER_MANAGE,
            ZONE_MANAGE,
            MONITORING
        )
    ),
    ADMIN(
        note = "관리자",
        authorities = listOf(
            ZONE_MANAGE,
            MONITORING
        )
    ),
    USER(
        note = "일반 사용자",
        authorities = listOf(
            MONITORING
        )
    )

}