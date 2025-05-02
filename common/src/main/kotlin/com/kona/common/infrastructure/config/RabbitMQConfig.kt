package com.kona.common.infrastructure.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.kona.common.infrastructure.util.CORRELATION_ID_HEADER_FIELD
import com.kona.common.infrastructure.util.getCorrelationId
import com.kona.common.infrastructure.util.setCorrelationId
import org.springframework.amqp.core.MessagePostProcessor
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RabbitMQConfig(
    private val objectMapper: ObjectMapper
) {

    @Bean
    fun defaultRabbitTemplate(connectionFactory: ConnectionFactory): RabbitTemplate {
        val messagePostProcessor = MessagePostProcessor {
            it.messageProperties.correlationId
            it.messageProperties.headers[CORRELATION_ID_HEADER_FIELD] = getCorrelationId()
            it
        }
        return RabbitTemplate(connectionFactory).apply {
            this.messageConverter = Jackson2JsonMessageConverter(objectMapper)
            this.setBeforePublishPostProcessors(messagePostProcessor)
        }
    }

    @Bean
    fun rabbitListenerContainerFactory(connectionFactory: ConnectionFactory): SimpleRabbitListenerContainerFactory {
        val messagePostProcessor = MessagePostProcessor {
            setCorrelationId(it.messageProperties.headers[CORRELATION_ID_HEADER_FIELD] as String?)
            it
        }
        return SimpleRabbitListenerContainerFactory().apply {
            setConnectionFactory(connectionFactory)
            setMessageConverter(Jackson2JsonMessageConverter(objectMapper))
            setAfterReceivePostProcessors(messagePostProcessor)
        }
    }

}