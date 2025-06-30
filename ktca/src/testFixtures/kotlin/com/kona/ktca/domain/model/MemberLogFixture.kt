package com.kona.ktca.domain.model

import com.kona.common.infrastructure.enumerate.MemberLogType

object MemberLogFixture {

    fun giveOne(
        member: Member,
        type: MemberLogType = MemberLogType.TRAFFIC_ZONE_CREATED,
        zone: TrafficZone = TrafficZoneFixture.giveOne()
    ): MemberLog {
        return MemberLog(
            member = member,
            type = type,
            zoneLog = zone
        )
    }
}