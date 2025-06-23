package com.kona.ktca.domain.model

import com.kona.common.infrastructure.enumerate.TrafficZoneGroupStatus

class TrafficZoneGroupFixture {

    fun giveOne(
        groupId: Long? = null,
        name: String,
        order: Int,
        status: TrafficZoneGroupStatus = TrafficZoneGroupStatus.ACTIVE
    ): TrafficZoneGroup = TrafficZoneGroup(
        groupId = groupId,
        name = name,
        order = order,
        status = status
    )

}