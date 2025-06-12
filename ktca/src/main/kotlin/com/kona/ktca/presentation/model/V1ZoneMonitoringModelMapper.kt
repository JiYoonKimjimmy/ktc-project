package com.kona.ktca.presentation.model

import com.kona.common.infrastructure.util.convertPatternOf
import com.kona.ktca.dto.V1ZoneMonitoringData
import com.kona.ktca.dto.V1ZoneMonitoringDataWaiting
import com.kona.ktca.dto.ZoneStatus
import com.kona.ktca.domain.model.TrafficZoneMonitor
import org.springframework.stereotype.Component

@Component
class V1ZoneMonitoringModelMapper {

    fun domainToModel(domain: TrafficZoneMonitor): V1ZoneMonitoringData {
        return V1ZoneMonitoringData(
            zoneId = domain.zoneId,
            zoneAlias = domain.zoneAlias,
            threshold = domain.threshold.toInt(),
            activationTime = domain.activationTime.convertPatternOf(),
            status = ZoneStatus.valueOf(domain.status.name),
            waiting = V1ZoneMonitoringDataWaiting(
                entryCount = domain.entryCount.toInt(),
                waitingCount = domain.waitingCount.toInt(),
                estimatedClearTime = domain.estimatedClearTime.toInt()
            )
        )
    }

}