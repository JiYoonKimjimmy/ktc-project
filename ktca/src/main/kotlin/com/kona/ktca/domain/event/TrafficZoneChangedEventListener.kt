package com.kona.ktca.domain.event

import com.kona.common.infrastructure.util.error
import com.kona.ktca.domain.port.inbound.MemberLogSavePort
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionalEventListener

@Component
class TrafficZoneChangedEventListener(
    private val defaultCoroutineScope: CoroutineScope,
    private val memberLogSavePort: MemberLogSavePort,
) {
    // logger
    private val logger = LoggerFactory.getLogger(this::class.java)

    @TransactionalEventListener
    fun handleTrafficZoneChangedEvent(event: TrafficZoneChangedEvent) = defaultCoroutineScope.launch {
        try {
            /**
             * [트래픽 Zone Command Event 처리]
             * - 트래픽 Zone 변경 log 저장 처리
             */
            memberLogSavePort.create(memberId = event.memberId, type = event.type, zone = event.zone)
                .also { logger.info("Saved Traffic Zone command log : memberId=${event.memberId}, logType=${event.type}") }
        } catch (e: Exception) {
            logger.error(e)
        }
    }

}