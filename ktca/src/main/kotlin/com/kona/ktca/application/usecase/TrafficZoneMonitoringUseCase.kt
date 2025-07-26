package com.kona.ktca.application.usecase

import com.kona.ktca.domain.model.TrafficZoneMonitor
import com.kona.ktca.domain.model.TrafficZoneStatsMonitor
import com.kona.ktca.domain.port.inbound.TrafficZoneMonitorCollectPort
import com.kona.ktca.domain.port.inbound.TrafficZoneMonitorFindPort
import com.kona.ktca.domain.port.inbound.TrafficZoneStatsMonitorPort
import com.kona.ktca.dto.StatsType
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Transactional(readOnly = true)
@Component
class TrafficZoneMonitoringUseCase(
    private val trafficZoneMonitorFindPort: TrafficZoneMonitorFindPort,
    private val trafficZoneMonitorCollectPort: TrafficZoneMonitorCollectPort,
    private val trafficZoneStatsMonitorPort: TrafficZoneStatsMonitorPort
) {

    /**
     * 트래픽 제어 Zone 모니터링 결과 조회
     */
    suspend fun trafficZoneMonitoring(zoneId: String?, groupId: String?): List<TrafficZoneMonitor> {
        return trafficZoneMonitorFindPort.findLatestMonitoring(zoneId, groupId)
    }

    /**
     * 트래픽 제어 Zone 모니터링 결과 수집
     */
    @Transactional
    suspend fun collectTrafficZoneMonitoring(zoneId: String?): List<TrafficZoneMonitor> {
        return trafficZoneMonitorCollectPort.collect(zoneId)
    }

    @Transactional
    suspend fun trafficZoneStatsMonitoring(statsType: StatsType, zoneId: String, startDate: String, endDate: String): List<TrafficZoneStatsMonitor> {
        return trafficZoneStatsMonitorPort.statsMonitor(statsType, zoneId, startDate, endDate)
    }

}