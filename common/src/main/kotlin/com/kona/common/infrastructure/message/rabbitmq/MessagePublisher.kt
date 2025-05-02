package com.kona.common.infrastructure.message.rabbitmq

interface MessagePublisher {

    suspend fun publishDirectMessage(exchange: MessageExchange, message: BaseMessage)

}