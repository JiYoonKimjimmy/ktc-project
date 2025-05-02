package com.kona.common.infrastructure.message.rabbitmq

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Component

@Component
class MessagePublisherImpl(
    private val defaultRabbitTemplate: RabbitTemplate
) : MessagePublisher {

    override suspend fun publishDirectMessage(exchange: MessageExchange, message: BaseMessage) = withContext(Dispatchers.IO) {
        defaultRabbitTemplate.convertAndSend(exchange.exchangeName, exchange.routingKey, message)
    }

}