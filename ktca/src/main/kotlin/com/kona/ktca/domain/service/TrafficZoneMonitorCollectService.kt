package com.kona.ktca.domain.service

import com.kona.common.infrastructure.enumerate.TrafficZoneStatus.ACTIVE
import com.kona.ktca.domain.dto.TrafficZoneDTO
import com.kona.ktca.domain.event.TrafficZoneMonitoringStoppedEvent
import com.kona.ktca.domain.model.TrafficZone
import com.kona.ktca.domain.model.TrafficZoneMonitor
import com.kona.ktca.domain.port.inbound.TrafficZoneMonitorCollectPort
import com.kona.ktca.domain.port.outbound.TrafficZoneCachingPort
import com.kona.ktca.domain.port.outbound.TrafficZoneMonitorCachingPort
import com.kona.ktca.domain.port.outbound.TrafficZoneMonitorRepository
import com.kona.ktca.domain.port.outbound.TrafficZoneRepository
import jakarta.transaction.Transactional
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service

@Service
class TrafficZoneMonitorCollectService(
    private val trafficZoneRepository: TrafficZoneRepository,
    private val trafficZoneCachingPort: TrafficZoneCachingPort,
    private val trafficZoneMonitorRepository: TrafficZoneMonitorRepository,
    private val trafficZoneMonitorCachingPort: TrafficZoneMonitorCachingPort,
    private val eventPublisher: ApplicationEventPublisher
) : TrafficZoneMonitorCollectPort {

    /**
     * 트래픽 Zone 모니터링 결과 수집
     * 1. 트래픽 제어 활성화 Zone 전체 조회
     * 2. 트래픽 Zone 현황 Cache 정보 조회
     * 3. 트래픽 Zone 모니터링 결과 전체 저장
     */
    @Transactional
    override suspend fun collect(zoneId: String?): List<TrafficZoneMonitor> {
        /**
         * [Zone 별 waitingCount 0 연속 횟수 확인]
         * - saveDBAndCache: 모니터링 조회 결과 `waitingCount: 0` 결과 5회 이하인 경우, DB 저장 후 Cache 저장
         * - saveOnlyCache: 모니터링 조회 결과 `waitingCount: 0` 결과 5회 이상인 경우, Cache 만 저장
         */
        val (saveDBAndCache, saveOnlyCache) = findAllTrafficZone(zoneId)
            .map { it.convertTrafficZoneMonitor() }
            .partition { it.checkMonitoringStopCounter() }

        // Zone 모니터링 결과 DB 저장
        val saved = saveDBAndCache.saveEntities()

        // Zone 모니터링 결과 Cache 저장
        val cached = (saved + saveOnlyCache).saveCaches()

        // only cache 모니터링 결과있는 경우, 모니터링 중단 Event 발행
        saveOnlyCache.publishTrafficZoneMonitoringStoppedEvent()

        return cached
    }

    private suspend fun findAllTrafficZone(zoneId: String?): List<TrafficZone> {
        return trafficZoneRepository.findAllByPredicate(dto = TrafficZoneDTO(zoneId = zoneId, status = ACTIVE))
    }

    private suspend fun TrafficZone.convertTrafficZoneMonitor(): TrafficZoneMonitor {
        trafficZoneCachingPort.findTrafficZoneWaiting(zone = this)
        return TrafficZoneMonitor.of(zone = this)
    }

    private suspend fun TrafficZoneMonitor.checkMonitoringStopCounter(): Boolean {
        val counter = trafficZoneMonitorCachingPort.incrementMonitoringStopCounter(zoneId = this.zoneId, isZero = this.isZeroWaitingCount())
        return counter <= 5
    }

    private suspend fun List<TrafficZoneMonitor>.saveEntities(): List<TrafficZoneMonitor> {
        return if (isNotEmpty()) {
            trafficZoneMonitorRepository.saveAll(monitoring = this)
        } else {
            emptyList()
        }
    }

    private suspend fun List<TrafficZoneMonitor>.saveCaches(): List<TrafficZoneMonitor> {
        return if (isNotEmpty()) {
            trafficZoneMonitorCachingPort.saveMonitoringLatestResult(monitoring = this)
        } else {
            emptyList()
        }
    }

    private suspend fun List<TrafficZoneMonitor>.publishTrafficZoneMonitoringStoppedEvent() {
        val zoneIds = map { it.zoneId }.distinct()
        if (zoneIds.isNotEmpty()) {
            eventPublisher.publishEvent(TrafficZoneMonitoringStoppedEvent(zoneIds))
        }
    }

}