package com.kona.ktca.domain.service

import com.kona.ktca.domain.model.TrafficZone
import com.kona.ktca.domain.model.TrafficZoneMonitor
import com.kona.ktca.domain.port.inbound.TrafficZoneMonitorCollectPort
import com.kona.ktca.domain.port.outbound.TrafficZoneFindPort
import com.kona.ktca.domain.port.outbound.TrafficZoneMonitorSavePort
import com.kona.ktca.domain.port.outbound.TrafficZoneWaitingFindPort
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class TrafficZoneMonitorCollectService(
    private val trafficZoneFindPort: TrafficZoneFindPort,
    private val trafficZoneWaitingFindPort: TrafficZoneWaitingFindPort,
    private val trafficZoneMonitorSavePort: TrafficZoneMonitorSavePort
) : TrafficZoneMonitorCollectPort {

    /**
     * 트래픽 Zone 모니터링 결과 수집
     * 1. 트래픽 제어 활성화 Zone 전체 조회
     * 2. 트래픽 Zone 현황 Cache 정보 조회
     * 3. 트래픽 Zone 모니터링 결과 전체 저장
     */
    @Transactional
    override suspend fun collect(zoneId: String?): List<TrafficZoneMonitor> {
        return findAllTrafficZone(zoneId)
            .generateTrafficZoneMonitoring()
            .saveTrafficZoneMonitoring()
    }

    private suspend fun findAllTrafficZone(zoneId: String?): List<TrafficZone> {
        return trafficZoneFindPort.findAllActiveTrafficZone(zoneId)
    }

    private suspend fun List<TrafficZone>.generateTrafficZoneMonitoring(): List<TrafficZoneMonitor> {
        return map {
            TrafficZoneMonitor.of(
                trafficZone = it,
                waiting = trafficZoneWaitingFindPort.findTrafficZoneWaiting(zoneId = it.zoneId, threshold = it.threshold)
            )
        }
    }

    private suspend fun List<TrafficZoneMonitor>.saveTrafficZoneMonitoring(): List<TrafficZoneMonitor> {
        return trafficZoneMonitorSavePort.saveAll(monitoring = this)
    }

}