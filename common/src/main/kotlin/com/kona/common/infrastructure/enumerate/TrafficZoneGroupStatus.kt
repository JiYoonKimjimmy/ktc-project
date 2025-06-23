package com.kona.common.infrastructure.enumerate

enum class TrafficZoneGroupStatus(
    private val note: String
) {
    ACTIVE(note = "트래픽 Zone 그룹 활성화 상태"),
    DELETED(note = "트래픽 Zone 그룹 삭제 상태")
}