package com.kona.ktc.v1.domain.service

import com.kona.ktc.v1.domain.event.SaveTrafficStatusEvent
import com.kona.ktc.v1.domain.model.TrafficToken
import com.kona.ktc.v1.domain.model.TrafficWaiting
import com.kona.ktc.v1.domain.port.inbound.TrafficWaitPort
import com.kona.ktc.v1.domain.port.outbound.TrafficControlPort
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service

@Service
class TrafficWaitService(
    private val trafficControlScriptExecuteAdapter: TrafficControlPort,
    private val eventPublisher: ApplicationEventPublisher
) : TrafficWaitPort {

    override suspend fun wait(token: TrafficToken): TrafficWaiting {
        return trafficControlScriptExecuteAdapter.controlTraffic(token)
            .also { eventPublisher.publishEvent(SaveTrafficStatusEvent(token = token, waiting = it)) }
    }

} 