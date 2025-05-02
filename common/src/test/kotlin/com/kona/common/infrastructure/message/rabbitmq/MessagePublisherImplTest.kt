package com.kona.common.infrastructure.message.rabbitmq

import com.kona.common.infrastructure.util.CORRELATION_ID_HEADER_FIELD
import com.kona.common.testsupport.rabbit.MockRabbitMQ
import com.kona.common.testsupport.rabbit.MockRabbitMQTestListener
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class MessagePublisherImplTest : StringSpec({

    listeners(MockRabbitMQTestListener(MockRabbitMQ.Exchange.V1_SAVE_TRAFFIC_STATUS_EXCHANGE))

    val mockRabbitTemplate = MockRabbitMQ.rabbitTemplate
    val messagePublisher = MessagePublisherImpl(mockRabbitTemplate)

    data class TestMessage(val message: String) : BaseMessage()

    "RabbitMQ Test Message 발행하여 결과 정상 확인한다" {
        // given
        val exchange = MessageExchange.V1_SAVE_TRAFFIC_STATUS_EXCHANGE
        val message = TestMessage("Hello World!!")

        // when
        messagePublisher.publishDirectMessage(exchange, message)

        // then
        val received = mockRabbitTemplate.receive(MessageQueue.V1_SAVE_TRAFFIC_STATUS_QUEUE)!!
        received.messageProperties.headers[CORRELATION_ID_HEADER_FIELD] shouldNotBe null

        val result = mockRabbitTemplate.messageConverter.fromMessage(received) as TestMessage
        result.message shouldBe "Hello World!!"
    }

})