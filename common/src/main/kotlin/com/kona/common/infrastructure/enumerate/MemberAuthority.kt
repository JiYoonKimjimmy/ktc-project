package com.kona.common.infrastructure.enumerate

enum class MemberAuthority(
    private val note: String
) {
    MEMBER_MANAGE(note = "관리자 정보 설정 권한"),
    ZONE_MANAGE(note = "Zone 정보 설정 권한"),
    MONITORING(note = "모니터링 조회 권한")
}