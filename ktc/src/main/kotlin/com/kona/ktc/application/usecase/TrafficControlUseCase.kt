package com.kona.ktc.application.usecase

import com.kona.ktc.domain.model.Traffic
import com.kona.ktc.domain.model.TrafficWaiting
import com.kona.ktc.domain.port.outbound.TrafficControlPort
import com.kona.ktc.domain.event.TrafficControlCompletedEvent
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class TrafficControlUseCase(
    private val trafficControlScriptExecuteAdapter: TrafficControlPort,
    private val eventPublisher: ApplicationEventPublisher
) {

    suspend fun controlTraffic(traffic: Traffic, now: Instant = Instant.now()): TrafficWaiting {
        return trafficControlScriptExecuteAdapter.controlTraffic(traffic, now)
            .validateTrafficWaitingResult()
            .also { publishTrafficControlCompletedEvent(traffic = traffic, waiting = it) }
    }

    private suspend fun publishTrafficControlCompletedEvent(traffic: Traffic, waiting: TrafficWaiting) {
        eventPublisher.publishEvent(TrafficControlCompletedEvent(traffic, waiting))
    }

}