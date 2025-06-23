package com.kona.ktca.domain.model

import com.kona.common.infrastructure.enumerate.MemberLogType

class MemberLogFixture {

    fun giveOne(memberId: Long, type: MemberLogType, zone: TrafficZone): MemberLog {
        return MemberLog(memberId = memberId, type = type).applyZoneLog(zone)
    }
}