package com.kona.ktca.application.event

import com.kona.common.infrastructure.util.error
import com.kona.ktca.domain.port.inbound.TrafficZoneEntryCountExpirePort
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class ExpireTrafficZoneEntryCountEventListener(
    private val defaultCoroutineScope: CoroutineScope,
    private val trafficZoneEntryCountExpirePort: TrafficZoneEntryCountExpirePort,
) {
    // logger
    private val logger = LoggerFactory.getLogger(this::class.java)

    @EventListener
    fun handleExpireTrafficZoneEntryCountEvent(
        event: ExpireTrafficZoneEntryCountEvent
    ) = defaultCoroutineScope.launch {
        try {
            async {
                trafficZoneEntryCountExpirePort.expireTrafficZoneEntryCount(event.zoneIds)
                    .also { logger.info("Expired Traffic Zone entry-count Count : $it") }
            }.await()
        } catch (e: Exception) {
            logger.error(e)
        }
    }
}