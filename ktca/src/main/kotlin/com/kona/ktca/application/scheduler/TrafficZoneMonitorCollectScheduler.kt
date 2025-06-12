package com.kona.ktca.application.scheduler

import com.kona.common.infrastructure.lock.DistributedLockManager
import com.kona.ktca.domain.port.inbound.TrafficZoneMonitorCollectPort
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Profile("!test")
@Component
class TrafficZoneMonitorCollectScheduler(
    private val trafficZoneMonitorCollectPort: TrafficZoneMonitorCollectPort,
    private val distributedLockManager: DistributedLockManager,
    private val defaultCoroutineScope: CoroutineScope
) {
    // logger
    private val logger = LoggerFactory.getLogger(this::class.java)

    /**
     * [트래픽 대기 Token 만료 처리]
     * - 매 5초마다 스케쥴링 실행
     */
    @Async("trafficZoneMonitorCollectSchedulerTaskExecutor")
    @Scheduled(fixedRate = 5000)
    fun handleTrafficZoneMonitorCollectScheduler() {
        defaultCoroutineScope.launch {
            try {
                /**
                 * [트래픽 토큰 만료 Scheduler]
                 * 1. 트래픽 Zone 모니터링 수집 task 실행 분산락 요청
                 * 2. 분산락 획득 후, 트래픽 Zone 대기 현황 수집 처리
                 */
                distributedLockManager.collectTrafficZoneMonitoringSchedulerLock {
                    trafficZoneMonitorCollectPort.collect()
                        .also { logger.info("Collected Traffic Zone Monitoring count : ${it.size}") }
                }
            } catch (e: Exception) {
                logger.error("Traffic Zone Monitoring Scheduler Failed.", e)
            }
        }
    }

}