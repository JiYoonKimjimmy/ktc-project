package com.kona.ktca.v1.domain.service

import com.kona.common.infrastructure.lock.DistributedLockManager
import com.kona.common.infrastructure.util.DATE_TIME_PATTERN_yyyyMMddHHmm
import com.kona.common.infrastructure.util.convertPatternOf
import com.kona.ktca.v1.domain.port.inbound.TrafficExpirePort
import com.kona.ktca.v1.domain.port.outbound.TrafficExpireExecutePort
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class TrafficExpireService(
    private val trafficExpireExecutePort: TrafficExpireExecutePort,
    private val distributedLockManager: DistributedLockManager,
) : TrafficExpirePort {
    // logger
    private val logger = LoggerFactory.getLogger(this::class.java)

    override suspend fun expireTraffic() {
        try {
            /**
             * [트래픽 토큰 만료 Scheduler]
             * 1. 트래픽 토큰 만료 task 실행 분산락 요청
             * 2. 분산락 획득 후, 현재 시간 - 1min 기준 트래픽 토큰 삭제 요청
             */
            val now = LocalDateTime.now().convertPatternOf(DATE_TIME_PATTERN_yyyyMMddHHmm)
            val result = distributedLockManager.expireTrafficTokenScheduleLock(now) {
                trafficExpireExecutePort.execute()
            }
            logger.info("Expired Traffic Token count : $result")
        } catch (e: Exception) {
            logger.error("Failed to expire traffic tokens", e)
        }
    }

}