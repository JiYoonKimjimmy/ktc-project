package com.kona.ktca.v1.application.scheduler

import com.kona.ktca.v1.domain.port.inbound.TrafficExpirePort
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

    @Async("trafficExpirationTaskExecutor")
    @Scheduled(fixedDelayString = "60000")
    fun handleExpireTrafficScheduler() {
        defaultCoroutineScope.launch {
            trafficExpirePort.expireTraffic()
        }
    }

}