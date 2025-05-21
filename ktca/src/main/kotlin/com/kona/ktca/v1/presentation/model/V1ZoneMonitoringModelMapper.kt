package com.kona.ktca.v1.presentation.model

import com.kona.common.infrastructure.util.convertPatternOf
import com.kona.ktca.dto.V1ZoneMonitoringData
import com.kona.ktca.dto.V1ZoneMonitoringDataWaiting
import com.kona.ktca.dto.ZoneStatus
import com.kona.ktca.v1.domain.model.TrafficZone
import org.springframework.stereotype.Component

@Component
class V1ZoneMonitoringModelMapper {

    fun domainToContent(domain: TrafficZone): V1ZoneMonitoringData {
        return V1ZoneMonitoringData(
            zoneId = domain.zoneId,
            zoneAlias = domain.zoneAlias,
            threshold = domain.threshold.toInt(),
            activationTime = domain.activationTime.convertPatternOf(),
            status = ZoneStatus.valueOf(domain.status.name),
            waiting = V1ZoneMonitoringDataWaiting(
                entryCount = domain.waiting.entryCount.toInt(),
                waitingCount = domain.waiting.waitingCount.toInt(),
                estimatedClearTime = domain.waiting.estimatedClearTime.toInt()
            )
        )
    }

}