package com.kona.common.testsupport.rabbit

import com.github.fridujo.rabbitmq.mock.compatibility.MockConnectionFactoryFactory
import com.kona.common.infrastructure.util.CORRELATION_ID_HEADER_FIELD
import com.kona.common.infrastructure.util.EMPTY
import com.kona.common.infrastructure.util.getCorrelationId
import org.springframework.amqp.core.*
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitAdmin
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter
import kotlin.reflect.KFunction2

object MockRabbitMQ {

    val rabbitTemplate: RabbitTemplate by lazy {
        val messagePostProcessor = MessagePostProcessor {
            it.messageProperties.headers[CORRELATION_ID_HEADER_FIELD] = getCorrelationId()
            it
        }
        RabbitTemplate(connectionFactory).apply {
            messageConverter = Jackson2JsonMessageConverter()
            this.setBeforePublishPostProcessors(messagePostProcessor)
        }
    }
    private val connectionFactory: ConnectionFactory by lazy {
        MockConnectionFactoryFactory
            .build()
            .enableConsistentHashPlugin()
            .let(::CachingConnectionFactory)
    }
    private val rabbitAdmin: RabbitAdmin by lazy { RabbitAdmin(connectionFactory) }

    fun binding(exchange: Exchange? = null) {
        if (exchange == null) {
            Exchange.entries.forEach { it.binding(rabbitAdmin) }
        } else {
            exchange.binding(rabbitAdmin)
        }
    }

    enum class Exchange(
        val exchangeName: String,
        val queueName: String,
        val routingKey: String,
        private val bindingFunction: KFunction2<Exchange, RabbitAdmin, Unit>,
        private val dlx: Exchange? = null,
        private val ttl: Long? = 500
    ) {

        TEST_DIRECT_EXCHANGE(
            exchangeName = "ktc.test.direct.exchange",
            queueName = "ktc.test.direct.queue",
            routingKey = "ktc.test.direct.routing.key",
            bindingFunction = Exchange::setupDirectExchange
        ),
        TEST_TOPIC_EXCHANGE(
            exchangeName = "ktc.test.topic.exchange",
            queueName = "ktc.test.topic.queue",
            routingKey = "ktc.test.topic.routing.key",
            bindingFunction = Exchange::setupTopicExchange
        ),
        TEST_FANOUT_EXCHANGE(
            exchangeName = "ktc.test.fanout.exchange",
            queueName = "ktc.test.fanout.queue",
            routingKey = EMPTY,
            bindingFunction = Exchange::setupFanoutExchange
        ),
        TEST_DL_DIRECT_DLX_EXCHANGE(
            exchangeName = "ktc.test.dl.direct.dl.exchange",
            queueName = "ktc.test.dl.direct.dl.queue",
            routingKey = "ktc.test.dl.direct.dl.routing.key",
            Exchange::setupDirectExchange
        ),
        TEST_DL_DIRECT_EXCHANGE(
            exchangeName = "ktc.test.dl.direct.exchange",
            queueName = "ktc.test.dl.direct.queue",
            routingKey = "ktc.test.dl.direct.routing.key",
            bindingFunction = Exchange::setupDeadLetterExchange,
            dlx = TEST_DL_DIRECT_DLX_EXCHANGE
        ),
        V1_SAVE_TRAFFIC_STATUS_EXCHANGE(
            exchangeName = "ktc.v1.save.traffic.status.exchange",
            queueName = "ktc.v1.save.traffic.status.queue",
            routingKey = "ktc.v1.save.traffic.status.routing-key",
            bindingFunction = Exchange::setupDirectExchange
        )
        ;

        fun binding(rabbitAdmin: RabbitAdmin) {
            bindingFunction.invoke(this, rabbitAdmin)
        }

        private fun setupDirectExchange(rabbitAdmin: RabbitAdmin) {
            val exchange = DirectExchange(exchangeName)
            val queue = Queue(queueName, false)
            rabbitAdmin.declareExchange(exchange)
            rabbitAdmin.declareQueue(queue)
            rabbitAdmin.declareBinding(BindingBuilder.bind(queue).to(exchange).with(routingKey))
        }

        private fun setupTopicExchange(rabbitAdmin: RabbitAdmin) {
            val exchange = TopicExchange(exchangeName)
            val queue = Queue(queueName, false)
            rabbitAdmin.declareExchange(exchange)
            rabbitAdmin.declareQueue(queue)
            rabbitAdmin.declareBinding(BindingBuilder.bind(queue).to(exchange).with(routingKey))
        }

        private fun setupFanoutExchange(rabbitAdmin: RabbitAdmin) {
            val exchange = FanoutExchange(exchangeName)
            val queue = Queue(queueName, false)
            rabbitAdmin.declareExchange(exchange)
            rabbitAdmin.declareQueue(queue)
            rabbitAdmin.declareBinding(BindingBuilder.bind(queue).to(exchange))
        }

        private fun setupDeadLetterExchange(rabbitAdmin: RabbitAdmin) {
            val exchange = DirectExchange(exchangeName)
            val queue = QueueBuilder.nonDurable(queueName)
                .withArgument("x-dead-letter-exchange", dlx?.exchangeName)
                .withArgument("x-dead-letter-routing-key", dlx?.routingKey)
                .withArgument("x-message-ttl", ttl)
                .build()
            rabbitAdmin.declareExchange(exchange)
            rabbitAdmin.declareQueue(queue)
            rabbitAdmin.declareBinding(BindingBuilder.bind(queue).to(exchange).with(routingKey))
        }

    }

}