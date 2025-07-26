package com.kona.ktca.presentation.model

import com.kona.common.infrastructure.util.convertPatternOf
import com.kona.ktca.domain.model.TrafficZoneMonitor
import com.kona.ktca.domain.model.TrafficZoneStatsMonitor
import com.kona.ktca.dto.StatsType
import com.kona.ktca.dto.V1ZoneMonitoringData
import com.kona.ktca.dto.V1ZoneMonitoringDataWaiting
import com.kona.ktca.dto.V1ZoneStatsMonitoringData
import com.kona.ktca.dto.V1ZoneStatsMonitoringResponse
import com.kona.ktca.dto.ZoneStatus
import org.springframework.stereotype.Component
import java.time.format.DateTimeFormatter

@Component
class V1ZoneMonitoringModelMapper {

    fun domainToModel(domain: TrafficZoneMonitor): V1ZoneMonitoringData {
        return V1ZoneMonitoringData(
            zoneId = domain.zoneId,
            zoneAlias = domain.zoneAlias,
            threshold = domain.threshold.toInt(),
            activationTime = domain.activationTime.convertPatternOf(),
            status = ZoneStatus.valueOf(domain.status.name),
            created = domain.created?.convertPatternOf(),
            updated = domain.updated?.convertPatternOf(),
            waiting = V1ZoneMonitoringDataWaiting(
                entryCount = domain.entryCount.toInt(),
                waitingCount = domain.waitingCount.toInt(),
                estimatedClearTime = domain.estimatedClearTime.toInt()
            )
        )
    }

    fun domainsToModel(domains: List<TrafficZoneStatsMonitor>, statsType: StatsType, zoneId:String): V1ZoneStatsMonitoringResponse {
        val content = domains.map { domain ->
            V1ZoneStatsMonitoringData(
                statsDate = domain.statsDate,
                maxThreshold = domain.maxThreshold.toInt(),
                totalEntryCount = domain.totalEntryCount.toInt(),
                maxWaitingCount = domain.maxWaitingCount.toInt(),
                maxEstimatedClearTime = domain.maxEstimatedClearTime.toInt()
            )
        }

        return domains.firstOrNull()?.let {
            V1ZoneStatsMonitoringResponse(
                zoneId = it.zoneId,
                zoneAlias = it.zoneAlias,
                statsType = statsType,
                content = content
            )
        } ?: V1ZoneStatsMonitoringResponse(
            zoneId = zoneId,
            zoneAlias = "NOT FOUND",
            statsType = statsType,
            content = emptyList()
        )
    }

}