package com.kona.ktca.domain.event

import com.kona.common.infrastructure.enumerate.MemberLogType
import com.kona.ktca.domain.model.TrafficZone

data class TrafficZoneChangedEvent(
    val memberId: Long,
    val type: MemberLogType,
    val zone: TrafficZone,
)