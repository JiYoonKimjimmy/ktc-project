package com.kona.common.testsupport.rabbit

import io.kotest.core.listeners.TestListener
import io.kotest.core.spec.Spec

class MockRabbitMQTestListener(
    private vararg val exchanges: MockRabbitMQ.Exchange = MockRabbitMQ.Exchange.entries.toTypedArray(),
) : TestListener {

    override suspend fun beforeSpec(spec: Spec) {
        exchanges.forEach(MockRabbitMQ::binding)
    }

}