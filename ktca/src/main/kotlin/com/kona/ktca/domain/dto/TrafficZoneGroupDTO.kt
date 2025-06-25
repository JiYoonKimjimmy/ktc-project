package com.kona.ktca.domain.dto

import com.kona.common.infrastructure.enumerate.TrafficZoneGroupStatus

data class TrafficZoneGroupDTO(
    val groupId: String? = null,
    val name: String? = null,
    val order: Int? = null,
    val status: TrafficZoneGroupStatus? = null
)