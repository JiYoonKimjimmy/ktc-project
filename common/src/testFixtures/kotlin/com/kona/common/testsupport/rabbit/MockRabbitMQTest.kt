package com.kona.common.testsupport.rabbit

import com.kona.common.infrastructure.util.EMPTY
import com.kona.common.testsupport.rabbit.MockRabbitMQ.Exchange.*
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class MockRabbitMQTest : StringSpec({

    listeners(MockRabbitMQTestListener())

    val rabbitTemplate = MockRabbitMQ.rabbitTemplate

    "'Direct' exchange 메시지 발행/수신 정상 확인한다" {
        // given
        val exchangeName = TEST_DIRECT_EXCHANGE.exchangeName
        val queueName = TEST_DIRECT_EXCHANGE.queueName
        val routingKey = TEST_DIRECT_EXCHANGE.routingKey
        val message = "Hello, 'Direct' World!"

        // when
        rabbitTemplate.convertAndSend(exchangeName, routingKey, message)

        // then
        rabbitTemplate.receiveAndConvert(queueName) shouldBe "Hello, 'Direct' World!"
    }

    "'Topic' exchange 메시지 발행/수신 정상 확인한다" {
        // given
        val exchangeName = TEST_TOPIC_EXCHANGE.exchangeName
        val queueName = TEST_TOPIC_EXCHANGE.queueName
        val routingKey = TEST_TOPIC_EXCHANGE.routingKey
        val message = "Hello, 'Topic' World!"

        // when
        rabbitTemplate.convertAndSend(exchangeName, routingKey, message)

        // then
        rabbitTemplate.receiveAndConvert(queueName) shouldBe "Hello, 'Topic' World!"
    }

    "'Fanout' exchange 메시지 발행/수신 정상 확인한다" {
        // given
        val exchangeName = TEST_FANOUT_EXCHANGE.exchangeName
        val queueName = TEST_FANOUT_EXCHANGE.queueName
        val message = "Hello, 'Fanout' World!"

        // when
        rabbitTemplate.convertAndSend(exchangeName, EMPTY, message)

        // then
        rabbitTemplate.receiveAndConvert(queueName) shouldBe "Hello, 'Fanout' World!"
    }

    "'Dead-Letter' exchange 메시지 발생/수신 정상 확인한다" {
        // given
        val exchangeName = TEST_DL_DIRECT_EXCHANGE.exchangeName
        val queueName = TEST_DL_DIRECT_EXCHANGE.queueName
        val routingKey = TEST_DL_DIRECT_EXCHANGE.routingKey
        val dlxQueueName = TEST_DL_DIRECT_DLX_EXCHANGE.queueName
        val message = "Hello, 'Dead-Letter' World!"

        // when
        rabbitTemplate.convertAndSend(exchangeName, routingKey, message)

        // then
        Thread.sleep(600)
        rabbitTemplate.receiveAndConvert(queueName) shouldBe null
        rabbitTemplate.receiveAndConvert(dlxQueueName) shouldBe "Hello, 'Dead-Letter' World!"
    }

})