package com.kona.ktca.presentation.model

import com.kona.common.infrastructure.util.convertPatternOf
import com.kona.ktca.dto.V1ZoneData
import com.kona.ktca.dto.ZoneStatus
import com.kona.ktca.domain.model.TrafficZone
import org.springframework.stereotype.Component

@Component
class V1ZoneModelMapper {

    fun domainToModel(trafficZone: TrafficZone): V1ZoneData {
        return V1ZoneData(
            zoneId = trafficZone.zoneId,
            zoneAlias = trafficZone.zoneAlias,
            threshold = trafficZone.threshold.toInt(),
            status = trafficZone.status.name.let(ZoneStatus::valueOf),
            activationTime = trafficZone.activationTime.convertPatternOf(),
            created = trafficZone.created?.convertPatternOf(),
            updated = trafficZone.updated?.convertPatternOf(),
        )
    }

}