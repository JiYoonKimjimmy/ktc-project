package com.kona.common.infrastructure.enumerate

enum class MemberLogType(
    private val note: String
) {

    TRAFFIC_ZONE_CREATED(note = "트래픽 Zone 생성 Event"),
    TRAFFIC_ZONE_UPDATED(note = "트래픽 Zone 수정 Event"),
    TRAFFIC_ZONE_DELETED(note = "트래픽 Zone 삭제 Event")

}