package com.kona.ktca.application.scheduler

import com.kona.common.infrastructure.lock.DistributedLockManager
import com.kona.ktca.domain.port.inbound.TrafficTokenExpirePort
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Profile("!test")
@Component
class TrafficTokenExpireScheduler(
    private val trafficTokenExpirePort: TrafficTokenExpirePort,
    private val distributedLockManager: DistributedLockManager,
    private val defaultCoroutineScope: CoroutineScope
) {
    // logger
    private val logger = LoggerFactory.getLogger(this::class.java)

    /**
     * [트래픽 대기 Token 만료 처리]
     * - 매 0초마다 스케쥴링 실행
     */
    @Async("trafficTokenExpireSchedulerTaskExecutor")
    @Scheduled(cron = "0 * * * * *")
    fun handleTrafficTokenExpireScheduler() {
        defaultCoroutineScope.launch {
            try {
                /**
                 * [트래픽 토큰 만료 Scheduler]
                 * 1. 트래픽 토큰 만료 task 실행 분산락 요청
                 * 2. 분산락 획득 후, 현재 시간 - 1min 기준 트래픽 토큰 삭제 요청
                 */
                distributedLockManager.expireTrafficTokenSchedulerLock {
                    trafficTokenExpirePort.expireTraffic()
                        .also { logger.info("Expired Traffic Token count : $it") }
                }
            } catch (e: Exception) {
                logger.error("Failed to expire traffic tokens", e)
            }

        }
    }

}