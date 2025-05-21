package com.kona.ktca.application.usecase

import com.kona.ktca.domain.model.TrafficZone
import com.kona.ktca.domain.port.inbound.TrafficZoneReadPort
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Transactional(readOnly = true)
@Component
class TrafficZoneMonitoringUseCase(
    private val trafficZoneReadPort: TrafficZoneReadPort
) {

    suspend fun trafficZoneMonitoring(zoneId: String? = null): List<TrafficZone> {
        return trafficZoneReadPort.findTrafficZones(zoneId, true)
    }

}