package com.kona.ktca.domain.model

import com.kona.common.infrastructure.enumerate.MemberLogType
import java.time.LocalDateTime

data class MemberLog(
    val logId: Long? = null,
    val member: Member,
    val type: MemberLogType,
    val zoneLog: TrafficZone,
    val created: LocalDateTime? = null,
    val updated: LocalDateTime? = null,
) {

    companion object {
        fun create(member: Member, type: MemberLogType, zoneLog: TrafficZone): MemberLog {
            return MemberLog(
                member = member,
                type = type,
                zoneLog = zoneLog
            )
        }
    }

}