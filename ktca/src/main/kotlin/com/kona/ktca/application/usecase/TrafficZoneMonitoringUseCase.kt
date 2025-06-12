package com.kona.ktca.application.usecase

import com.kona.ktca.domain.model.TrafficZoneMonitor
import com.kona.ktca.domain.port.inbound.TrafficZoneMonitorCollectPort
import com.kona.ktca.domain.port.inbound.TrafficZoneMonitorReadPort
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Transactional(readOnly = true)
@Component
class TrafficZoneMonitoringUseCase(
    private val trafficZoneMonitorCollectPort: TrafficZoneMonitorCollectPort,
    private val trafficZoneMonitorReadPort: TrafficZoneMonitorReadPort
) {

    /**
     * 트래픽 제어 Zone 모니터링 결과 조회
     */
    suspend fun trafficZoneMonitoring(zoneId: String? = null): List<TrafficZoneMonitor> {
        return trafficZoneMonitorReadPort.findLatestMonitoring(zoneId)
    }

    /**
     * 트래픽 제어 Zone 모니터링 결과 수집
     */
    @Transactional
    suspend fun collectTrafficZoneMonitoring(zoneId: String? = null): List<TrafficZoneMonitor> {
        return trafficZoneMonitorCollectPort.collect(zoneId)
    }

}