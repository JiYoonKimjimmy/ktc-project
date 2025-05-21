package com.kona.ktc.application.usecase

import com.kona.ktc.infrastructure.event.SaveTrafficStatusEvent
import com.kona.ktc.domain.model.Traffic
import com.kona.ktc.domain.model.TrafficWaiting
import com.kona.ktc.domain.port.outbound.TrafficControlPort
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
            .also { publishSaveTrafficStatusEvent(traffic = traffic, waiting = it) }
    }

    private suspend fun publishSaveTrafficStatusEvent(traffic: Traffic, waiting: TrafficWaiting) {
        eventPublisher.publishEvent(SaveTrafficStatusEvent(traffic, waiting))
    }

}