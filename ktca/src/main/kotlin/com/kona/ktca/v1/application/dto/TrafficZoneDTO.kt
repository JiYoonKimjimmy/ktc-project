package com.kona.ktca.v1.application.dto

import com.kona.common.infrastructure.enumerate.TrafficZoneStatus
import com.kona.ktca.v1.domain.model.TrafficZone
import java.time.LocalDateTime

data class TrafficZoneDTO(
    val zoneId: String? = null,
    val zoneAlias: String? = null,
    val threshold: Long? = null,
    val activationTime: LocalDateTime? = null,
    val status: TrafficZoneStatus? = null,
) {
    val isCreate: Boolean by lazy { zoneId == null }

    fun toDomain(): TrafficZone {
        return TrafficZone(
            zoneAlias = zoneAlias!!,
            threshold = threshold!!,
            activationTime = activationTime!!,
            status = status!!
        )
    }

}