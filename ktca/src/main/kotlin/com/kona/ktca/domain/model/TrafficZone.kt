package com.kona.ktca.domain.model

import com.kona.common.infrastructure.enumerate.TrafficZoneStatus
import com.kona.common.infrastructure.enumerate.TrafficZoneStatus.DELETED
import com.kona.common.infrastructure.util.SnowflakeIdGenerator
import com.kona.common.infrastructure.util.TRAFFIC_ZONE_ID_PREFIX
import com.kona.ktca.domain.dto.TrafficZoneDTO
import java.time.LocalDateTime

data class TrafficZone(
    val zoneId: String,
    val zoneAlias: String,
    val threshold: Long,
    val status: TrafficZoneStatus,
    val activationTime: LocalDateTime,
    val created: LocalDateTime? = null,
    val updated: LocalDateTime? = null,
    val group: TrafficZoneGroup? = null,
) {

    lateinit var waiting: TrafficZoneWaiting

    val groupId: String by lazy { group!!.groupId }

    companion object {
        fun create(dto: TrafficZoneDTO): TrafficZone {
            return TrafficZone(
                zoneId = dto.zoneId ?: generateZoneId(),
                zoneAlias = dto.zoneAlias!!,
                threshold = dto.threshold!!,
                status = dto.status ?: TrafficZoneStatus.ACTIVE,
                activationTime = dto.activationTime ?: LocalDateTime.now(),
                group = dto.group
            )
        }

        private fun generateZoneId(): String {
            return TRAFFIC_ZONE_ID_PREFIX + SnowflakeIdGenerator.generate()
        }
    }

    fun applyWaiting(waiting: TrafficZoneWaiting): TrafficZone {
        this.waiting = waiting
        return this
    }

    fun update(dto: TrafficZoneDTO): TrafficZone {
        return copy(
            zoneAlias = dto.zoneAlias ?: zoneAlias,
            threshold = dto.threshold ?: threshold,
            status = dto.status ?: status,
            activationTime = dto.activationTime ?: activationTime,
            group = dto.group ?: group
        )
    }

    fun delete(): TrafficZone {
        return this.copy(status = DELETED)
    }

}