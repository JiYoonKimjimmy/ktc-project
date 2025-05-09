package com.kona.ktca.v1.application.scheduler

import com.kona.common.infrastructure.lock.DistributedLockManager
import com.kona.common.infrastructure.util.DATE_TIME_PATTERN_yyyyMMddHHmm
import com.kona.common.infrastructure.util.convertPatternOf
import com.kona.ktca.v1.domain.port.inbound.TrafficExpirePort
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class TrafficExpirationScheduler(
    private val trafficExpirePort: TrafficExpirePort,
    private val defaultCoroutineScope: CoroutineScope
) {

    @Async("trafficExpirationTaskExecutor")
    @Scheduled(fixedDelayString = "60000")
    fun handleExpireTrafficScheduler() {
        defaultCoroutineScope.launch {
            trafficExpirePort.expireTraffic()
        }
    }

}