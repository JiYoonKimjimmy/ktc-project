package com.kona.ktc.v1.domain.service

import com.kona.ktc.v1.domain.event.SaveTrafficStatusEvent
import com.kona.ktc.v1.domain.model.TrafficToken
import com.kona.ktc.v1.domain.model.TrafficWaiting
import com.kona.ktc.v1.domain.port.inbound.TrafficEntryPort
import com.kona.ktc.v1.domain.port.outbound.TrafficControlPort
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class TrafficEntryService(
    private val trafficControlScriptExecuteAdapter: TrafficControlPort,
    private val eventPublisher: ApplicationEventPublisher
) : TrafficEntryPort {

    override suspend fun entry(token: TrafficToken, now: Instant): TrafficWaiting {
        return trafficControlScriptExecuteAdapter.controlTraffic(token, now)
            .also { eventPublisher.publishEvent(SaveTrafficStatusEvent(token = token, waiting = it)) }
    }

}