package com.kona.ktc.v1.domain.service

import com.kona.ktc.v1.domain.event.SaveTrafficStatusEvent
import com.kona.ktc.v1.domain.model.Traffic
import com.kona.ktc.v1.domain.model.TrafficWaiting
import com.kona.ktc.v1.domain.port.inbound.TrafficWaitPort
import com.kona.ktc.v1.domain.port.outbound.TrafficControlPort
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class TrafficWaitService(
    private val trafficControlScriptExecuteAdapter: TrafficControlPort,
    private val eventPublisher: ApplicationEventPublisher
) : TrafficWaitPort {

    override suspend fun wait(traffic: Traffic, now: Instant): TrafficWaiting {
        return trafficControlScriptExecuteAdapter.controlTraffic(traffic, now)
            .also { eventPublisher.publishEvent(SaveTrafficStatusEvent(traffic = traffic, waiting = it)) }
    }

} 