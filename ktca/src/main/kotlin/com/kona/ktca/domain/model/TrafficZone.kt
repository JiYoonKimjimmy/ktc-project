package com.kona.ktca.domain.model

import com.kona.common.infrastructure.enumerate.TrafficZoneStatus
import com.kona.common.infrastructure.enumerate.TrafficZoneStatus.DELETED
import com.kona.common.infrastructure.util.SnowflakeIdGenerator
import com.kona.common.infrastructure.util.TRAFFIC_ZONE_ID_PREFIX
import com.kona.ktca.domain.dto.TrafficZoneDTO
import java.time.LocalDateTime

data class TrafficZone(
    val zoneId: String = generateZoneId(),
    val zoneAlias: String,
    val threshold: Long,
    val status: TrafficZoneStatus,
    val activationTime: LocalDateTime,
    val created: LocalDateTime? = null,
    val updated: LocalDateTime? = null,
) {
    lateinit var waiting: TrafficZoneWaiting

    companion object {

        fun create(dto: TrafficZoneDTO): TrafficZone {
            return TrafficZone(
                zoneId = dto.zoneId ?: generateZoneId(),
                zoneAlias = dto.zoneAlias!!,
                threshold = dto.threshold!!,
                activationTime = dto.activationTime ?: LocalDateTime.now(),
                status = dto.status ?: TrafficZoneStatus.ACTIVE
            )
        }

        private fun generateZoneId(): String {
            return TRAFFIC_ZONE_ID_PREFIX + SnowflakeIdGenerator.generate()
        }

    }

    suspend fun applyWaiting(function: suspend (String, Long) -> TrafficZoneWaiting): TrafficZone {
        this.waiting = function(zoneId, threshold)
        return this
    }

    fun update(dto: TrafficZoneDTO): TrafficZone {
        return this.copy(
            zoneAlias = dto.zoneAlias ?: zoneAlias,
            threshold = dto.threshold ?: threshold,
            activationTime = dto.activationTime ?: activationTime,
            status = dto.status ?: status
        )
    }

    fun delete(): TrafficZone {
        return this.copy(status = DELETED)
    }

}