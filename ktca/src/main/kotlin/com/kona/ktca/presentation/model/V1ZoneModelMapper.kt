package com.kona.ktca.presentation.model

import com.kona.common.infrastructure.util.convertPatternOf
import com.kona.ktca.dto.V1ZoneData
import com.kona.ktca.dto.ZoneStatus
import com.kona.ktca.domain.model.TrafficZone
import org.springframework.stereotype.Component

@Component
class V1ZoneModelMapper {

    fun domainToModel(zone: TrafficZone): V1ZoneData {
        return V1ZoneData(
            zoneId = zone.zoneId,
            zoneAlias = zone.zoneAlias,
            threshold = zone.threshold.toInt(),
            status = zone.status.name.let(ZoneStatus::valueOf),
            activationTime = zone.activationTime.convertPatternOf(),
            created = zone.created?.convertPatternOf(),
            updated = zone.updated?.convertPatternOf(),
        )
    }

}