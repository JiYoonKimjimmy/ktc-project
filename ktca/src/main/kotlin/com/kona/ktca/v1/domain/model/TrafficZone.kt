package com.kona.ktca.v1.domain.model

import com.kona.common.infrastructure.enumerate.TrafficZoneStatus
import com.kona.common.infrastructure.enumerate.TrafficZoneStatus.DELETED
import com.kona.common.infrastructure.error.ErrorCode
import com.kona.common.infrastructure.error.exception.InternalServiceException
import com.kona.common.infrastructure.util.SnowflakeIdGenerator
import com.kona.common.infrastructure.util.TRAFFIC_ZONE_ID_PREFIX
import com.kona.ktca.v1.domain.port.dto.TrafficZoneDTO
import java.time.LocalDateTime

data class TrafficZone(
    val zoneId: String = generateZoneId(),
    val zoneAlias: String,
    val threshold: Long,
    val activationTime: LocalDateTime,
    val status: TrafficZoneStatus,
    var waiting: TrafficZoneWaiting = TrafficZoneWaiting(),
    var isUpdate: Boolean = false
) {

    companion object {
        fun generateZoneId(): String {
            return TRAFFIC_ZONE_ID_PREFIX + SnowflakeIdGenerator.generate()
        }
    }

    fun update(dto: TrafficZoneDTO): TrafficZone {
        if (dto.status != null && status == DELETED) {
            throw InternalServiceException(ErrorCode.DELETED_TRAFFIC_ZONE_STATUS_NOT_CHANGED)
        }
        return this.copy(
            zoneAlias = dto.zoneAlias ?: zoneAlias,
            threshold = dto.threshold ?: threshold,
            activationTime = dto.activationTime ?: activationTime,
            status = dto.status ?: status
        )
    }

    suspend fun applyWaiting(function: suspend (String, Long) -> TrafficZoneWaiting): TrafficZone {
        this.waiting = function(zoneId, threshold)
        return this
    }

}