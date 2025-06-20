package com.kona.common.infrastructure.enumerate

import com.kona.common.infrastructure.enumerate.MemberAuthority.*

enum class MemberRole(
    private val note: String,
    private val authorities: List<MemberAuthority>
) {

    ADMINISTRATOR(
        note = "최고 관리자",
        authorities = listOf(
            MEMBER_MANAGE,
            ZONE_MANAGE,
            MONITORING
        )
    ),
    MANAGER(
        note = "중간 관리자",
        authorities = listOf(
            ZONE_MANAGE,
            MONITORING
        )
    ),
    VIEWER(
        note = "일반(조회) 관리자",
        authorities = listOf(
            MONITORING
        )
    )

}