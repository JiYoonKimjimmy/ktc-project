package com.kona.ktca.domain.dto

import com.kona.common.infrastructure.enumerate.TrafficZoneStatus
import com.kona.common.infrastructure.enumerate.TrafficZoneStatus.ACTIVE
import com.kona.ktca.domain.model.TrafficZoneGroup
import java.time.LocalDateTime

object TrafficZoneDTOFixture {

    fun giveOne(
        zoneId: String? = null,
        zoneAlias: String? = null,
        threshold: Long? = null,
        groupId: String? = null,
        status: TrafficZoneStatus? = null,
        activationTime: LocalDateTime? = null,
        group: TrafficZoneGroup? = null,
    ): TrafficZoneDTO {
        val dto = TrafficZoneDTO(
            zoneId = zoneId,
            zoneAlias = zoneAlias,
            threshold = threshold,
            groupId = groupId,
            status = status,
            activationTime = activationTime,
        )
        if (group != null) dto.applyGroup(group)
        return dto
    }

}