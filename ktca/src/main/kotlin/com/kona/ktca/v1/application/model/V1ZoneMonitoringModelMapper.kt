package com.kona.ktca.v1.application.model

import com.kona.common.infrastructure.util.convertPatternOf
import com.kona.ktca.dto.V1ZoneMonitoringData
import com.kona.ktca.dto.V1ZoneMonitoringDataWaiting
import com.kona.ktca.dto.ZoneStatus
import com.kona.ktca.v1.domain.model.TrafficZone
import org.springframework.stereotype.Component

@Component
class V1ZoneMonitoringModelMapper {

    fun domainToContent(domain: List<TrafficZone>): List<V1ZoneMonitoringData> {
        return domain.map {
            V1ZoneMonitoringData(
                zoneId = it.zoneId,
                zoneAlias = it.name,
                threshold = it.threshold.toInt(),
                activationTime = it.activationTime.convertPatternOf(),
                status = ZoneStatus.valueOf(it.status.name),
                waiting = V1ZoneMonitoringDataWaiting(
                    entryCount = it.waiting.entryCount.toInt(),
                    waitingCount = it.waiting.waitingCount.toInt(),
                    estimatedClearTime = it.waiting.estimatedClearTime.toInt()
                ),
            )
        }
    }

}