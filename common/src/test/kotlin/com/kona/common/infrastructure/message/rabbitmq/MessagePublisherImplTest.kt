package com.kona.common.infrastructure.message.rabbitmq

import com.kona.common.infrastructure.message.rabbitmq.dto.V1TrafficStatusMessage
import com.kona.common.infrastructure.util.CORRELATION_ID_HEADER_FIELD
import com.kona.common.testsupport.rabbit.MockRabbitMQ
import com.kona.common.testsupport.rabbit.MockRabbitMQTestListener
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class MessagePublisherImplTest : StringSpec({

    listeners(MockRabbitMQTestListener(MockRabbitMQ.Exchange.V1_SAVE_TRAFFIC_STATUS_EXCHANGE))

    val mockRabbitTemplate = MockRabbitMQ.rabbitTemplate
    val messagePublisher = MessagePublisherImpl(mockRabbitTemplate, true)

    "V1TrafficStatusMessage 발행 결과 정상 확인한다" {
        // given
        val exchange = MessageExchange.V1_SAVE_TRAFFIC_STATUS_EXCHANGE
        val message = V1TrafficStatusMessage(
            zoneId = "TEST_ZONE",
            token = "token-1",
            clientIp = "127.0.0.1",
            clientAgent = "ANDROID",
            waitingNumber = 1,
            estimatedTime = 60000,
            totalCount = 1
        )

        // when
        messagePublisher.publishDirectMessage(exchange, message)

        // then
        Thread.sleep(500)
        val received = mockRabbitTemplate.receive(MessageQueue.V1_SAVE_TRAFFIC_STATUS_QUEUE)!!
        received.messageProperties.headers[CORRELATION_ID_HEADER_FIELD] shouldNotBe null

        val result = mockRabbitTemplate.messageConverter.fromMessage(received) as V1TrafficStatusMessage
        result.zoneId shouldBe "TEST_ZONE"
        result.token shouldBe "token-1"
        result.clientIp shouldBe "127.0.0.1"
        result.clientAgent shouldBe "ANDROID"
        result.waitingNumber shouldBe 1
        result.estimatedTime shouldBe 60000
        result.totalCount shouldBe 1
    }

})