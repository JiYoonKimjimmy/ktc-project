package com.kona.ktc.v1.domain.service

import com.kona.common.infrastructure.message.rabbitmq.MessageExchange.V1_SAVE_TRAFFIC_STATUS_EXCHANGE
import com.kona.common.infrastructure.message.rabbitmq.MessagePublisherImpl
import com.kona.common.testsupport.coroutine.TestCoroutineScope
import com.kona.common.testsupport.rabbit.MockRabbitMQ
import com.kona.ktc.v1.domain.event.SaveTrafficStatusEvent
import kotlinx.coroutines.launch
import org.springframework.context.ApplicationEventPublisher

class FakeApplicationEventPublisher : ApplicationEventPublisher {

    private val defaultCoroutineScope = TestCoroutineScope.defaultCoroutineScope
    private val messagePublisher = MessagePublisherImpl(MockRabbitMQ.rabbitTemplate)

    override fun publishEvent(event: Any) {
        defaultCoroutineScope.launch {
            when (event) {
                is SaveTrafficStatusEvent -> messagePublisher.publishDirectMessage(exchange = V1_SAVE_TRAFFIC_STATUS_EXCHANGE, message = event.message)
            }
        }
    }

}