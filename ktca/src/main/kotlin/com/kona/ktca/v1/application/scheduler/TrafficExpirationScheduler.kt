package com.kona.ktca.v1.application.scheduler

import com.kona.common.infrastructure.lock.DistributedLockManager
import com.kona.common.infrastructure.util.DATE_TIME_PATTERN_yyyyMMddHHmm
import com.kona.common.infrastructure.util.convertPatternOf
import com.kona.ktca.v1.domain.port.outbound.TrafficExpirePort
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class TrafficExpirationScheduler(
    private val distributedLockManager: DistributedLockManager,
    private val trafficExpirePort: TrafficExpirePort,
) {
    // logger
    private val logger = LoggerFactory.getLogger(this::class.java)

    @Async("trafficExpirationTaskExecutor")
    @Scheduled(fixedDelayString = "60000")
    fun expireTrafficScheduler() = runBlocking {
        /**
         * [트래픽 토큰 만료 Scheduler]
         * 1. 트래픽 토큰 만료 task 실행 분산락 요청
         * 2. 분산락 획득 후, 현재 시간 - 1min 기준 트래픽 토큰 삭제 요청
         */
        val now = LocalDateTime.now().convertPatternOf(DATE_TIME_PATTERN_yyyyMMddHHmm)
        val result = distributedLockManager.expireTrafficTokenScheduleLock(now) {
            trafficExpirePort.expireTraffic()
        }
        logger.info("Expired Traffic Token count : $result")
    }

}