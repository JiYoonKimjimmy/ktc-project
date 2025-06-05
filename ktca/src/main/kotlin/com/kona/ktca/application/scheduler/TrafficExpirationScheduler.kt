package com.kona.ktca.application.scheduler

import com.kona.ktca.domain.port.inbound.TrafficExpirePort
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class TrafficExpirationScheduler(
    private val trafficExpirePort: TrafficExpirePort,
    private val defaultCoroutineScope: CoroutineScope
) {

    /**
     * [트래픽 대기 Token 만료 처리]
     * - 매 0초마다 스케쥴링 실행
     */
    @Async("trafficExpirationTaskExecutor")
    @Scheduled(cron = "* * * * * *")
    fun handleExpireTrafficScheduler() {
        defaultCoroutineScope.launch {
            trafficExpirePort.expireTraffic()
        }
    }

}