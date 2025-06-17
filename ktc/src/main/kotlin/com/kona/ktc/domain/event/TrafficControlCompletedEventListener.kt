package com.kona.ktc.domain.event

import com.kona.common.infrastructure.message.rabbitmq.MessageExchange.V1_SAVE_TRAFFIC_STATUS_EXCHANGE
import com.kona.common.infrastructure.message.rabbitmq.MessagePublisher
import com.kona.common.infrastructure.util.error
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class TrafficControlCompletedEventListener(
    private val defaultCoroutineScope: CoroutineScope,
    private val messagePublisher: MessagePublisher,
) {
    // logger
    private val logger = LoggerFactory.getLogger(this::class.java)

    @EventListener
    fun handleTrafficControlCompletedEvent(event: TrafficControlCompletedEvent) = defaultCoroutineScope.launch {
        try {
            async {
                messagePublisher.publishDirectMessage(exchange = V1_SAVE_TRAFFIC_STATUS_EXCHANGE, message = event.message)
            }.await()
        } catch (e: Exception) {
            logger.error(e)
        }
    }
} 