package com.kona.ktca.domain.event

import com.kona.common.infrastructure.util.error
import com.kona.ktca.domain.port.inbound.TrafficZoneEntryCountExpirePort
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionalEventListener

@Component
class TrafficZoneMonitoringStoppedEventListener(
    private val defaultCoroutineScope: CoroutineScope,
    private val trafficZoneEntryCountExpirePort: TrafficZoneEntryCountExpirePort,
) {
    // logger
    private val logger = LoggerFactory.getLogger(this::class.java)

    @TransactionalEventListener
    fun handleTrafficZoneMonitoringStoppedEvent(
        event: TrafficZoneMonitoringStoppedEvent
    ) = defaultCoroutineScope.launch {
        try {
            async {
                /**
                 * [트래픽 Zone 모니터링 중단 Event 처리]
                 * - 모니터링 중단 트래픽 Zone `entry-count` Cache 만료 처리
                 */
                trafficZoneEntryCountExpirePort.expireTrafficZoneEntryCount(event.zoneIds)
                .also { logger.info("Expired Traffic Zone entry-count Count : $it") }
            }.await()
        } catch (e: Exception) {
            logger.error(e)
        }
    }
}