package com.kona.ktca.domain.model

import com.kona.common.infrastructure.enumerate.TrafficZoneGroupStatus
import com.kona.ktca.domain.dto.TrafficZoneGroupDTO
import java.time.LocalDateTime

data class TrafficZoneGroup(
    val groupId: Long? = null,
    val name: String,
    val order: Int,
    val status: TrafficZoneGroupStatus,
    val created: LocalDateTime? = null,
    val updated: LocalDateTime? = null
) {

    fun update(dto: TrafficZoneGroupDTO): TrafficZoneGroup {
        return copy(
            name = dto.name ?: name,
            order = dto.order ?: order,
            status = dto.status ?: status
        )
    }

}