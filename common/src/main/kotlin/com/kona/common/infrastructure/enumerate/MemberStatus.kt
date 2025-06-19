package com.kona.common.infrastructure.enumerate

enum class MemberStatus(
    private val note: String
) {
    ACTIVE(note = "활성화 상태"),
    INACTIVE(note = "비활성화 상태"),
    DELETED(note = "삭제 상태")
}