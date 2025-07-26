package com.kona.ktca.domain.event

import com.kona.ktca.domain.port.inbound.TrafficZoneStatsMonitorPort
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Component
class TrafficZoneMonitorSavedEventListener(
    private val defaultCoroutineScope: CoroutineScope,
    private val trafficZoneStatsMonitorPort: TrafficZoneStatsMonitorPort
) {

    @EventListener
    @Transactional
    fun handle(event: TrafficZoneMonitorSavedEvent) = defaultCoroutineScope.launch {
        trafficZoneStatsMonitorPort.applySaveEvent(LocalDateTime.now(), event)
    }
}