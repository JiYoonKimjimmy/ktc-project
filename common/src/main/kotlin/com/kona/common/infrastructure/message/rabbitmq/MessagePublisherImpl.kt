package com.kona.common.infrastructure.message.rabbitmq

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class MessagePublisherImpl(

    private val defaultRabbitTemplate: RabbitTemplate,

    @Value("\${message.publish.rabbit.enabled}")
    private val rabbitEnabled: Boolean,

) : MessagePublisher {

    override suspend fun publishDirectMessage(exchange: MessageExchange, message: BaseMessage) = withContext(Dispatchers.IO) {
        if (rabbitEnabled) {
            defaultRabbitTemplate.convertAndSend(exchange.exchangeName, exchange.routingKey, message)
        }
    }

}