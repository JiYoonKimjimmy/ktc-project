package com.kona.ktca.v1.presentation.model

import com.kona.common.infrastructure.util.convertPatternOf
import com.kona.ktca.dto.V1ZoneData
import com.kona.ktca.dto.ZoneStatus
import com.kona.ktca.v1.domain.model.TrafficZone
import org.springframework.stereotype.Component

@Component
class V1ZoneModelMapper {

    fun domainToModel(trafficZone: TrafficZone): V1ZoneData {
        return V1ZoneData(
            zoneId = trafficZone.zoneId,
            zoneAlias = trafficZone.zoneAlias,
            threshold = trafficZone.threshold.toInt(),
            activationTime = trafficZone.activationTime.convertPatternOf(),
            status = ZoneStatus.valueOf(trafficZone.status.name),
            created = trafficZone.activationTime.convertPatternOf(),
            updated = trafficZone.activationTime.convertPatternOf(),
        )
    }


}