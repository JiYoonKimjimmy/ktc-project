package com.kona.common.infrastructure.enumerate

enum class TrafficZoneStatus(
    private val note: String
) {
    ACTIVE(note = "트래픽 Zone 제어 활성화 상태"),
    BLOCKED(note = "트래픽 Zone 제어 차단 상태"),
    DELETED(note = "트래픽 Zone 삭제 상태")
}