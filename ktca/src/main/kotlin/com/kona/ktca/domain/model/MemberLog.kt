package com.kona.ktca.domain.model

import com.kona.common.infrastructure.enumerate.MemberLogType

data class MemberLog(
    val type: MemberLogType,
    val memberId: Long,
    val zone: TrafficZone
)