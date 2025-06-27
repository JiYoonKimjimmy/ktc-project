package com.kona.ktca.domain.model

import com.kona.common.infrastructure.enumerate.TrafficZoneGroupStatus
import com.kona.common.infrastructure.util.SnowflakeIdGenerator
import com.kona.common.infrastructure.util.TRAFFIC_GROUP_ID_PREFIX
import com.kona.common.infrastructure.util.ZERO
import com.kona.ktca.domain.dto.TrafficZoneGroupDTO
import java.time.LocalDateTime

data class TrafficZoneGroup(
    val groupId: String,
    val name: String,
    val order: Int,
    val status: TrafficZoneGroupStatus,
    val created: LocalDateTime? = null,
    val updated: LocalDateTime? = null
) {

    companion object {
        fun create(name: String, order: Int? = null): TrafficZoneGroup {
            return TrafficZoneGroup(
                groupId = generateGroupId(),
                name = name,
                order = order ?: ZERO.toInt(),
                status = TrafficZoneGroupStatus.ACTIVE
            )
        }

        private fun generateGroupId(): String {
            return "$TRAFFIC_GROUP_ID_PREFIX${SnowflakeIdGenerator.generate()}"
        }
    }

    fun update(dto: TrafficZoneGroupDTO): TrafficZoneGroup {
        return copy(
            name = dto.name ?: name,
            order = dto.order ?: order,
            status = dto.status ?: status
        )
    }

}