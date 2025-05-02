package com.kona.common.infrastructure.message.rabbitmq

import com.kona.common.infrastructure.util.EMPTY

enum class MessageExchange(
    val exchangeName: String,
    val routingKey: String = EMPTY
) {

    V1_SAVE_TRAFFIC_STATUS_EXCHANGE(
        exchangeName = "ktc.v1.save.traffic.status.exchange",
        routingKey = "ktc.v1.save.traffic.status.routing-key"
    )

}

class MessageQueue {

    companion object {
        const val V1_SAVE_TRAFFIC_STATUS_QUEUE = "ktc.v1.save.traffic.status.queue"
    }

}