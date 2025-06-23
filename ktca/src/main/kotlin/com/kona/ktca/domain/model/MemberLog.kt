package com.kona.ktca.domain.model

import com.kona.common.infrastructure.enumerate.MemberLogType
import java.time.LocalDateTime

data class MemberLog(
    val logId: Long? = null,
    val memberId: Long,
    val type: MemberLogType,
    val created: LocalDateTime? = null,
    val updated: LocalDateTime? = null,
) {
    lateinit var zoneLog: TrafficZone
    lateinit var member: Member

    fun applyZoneLog(zone: TrafficZone): MemberLog {
        this.zoneLog = zone
        return this
    }

    fun applyMember(member: Member): MemberLog {
        this.member = member
        return this
    }
}