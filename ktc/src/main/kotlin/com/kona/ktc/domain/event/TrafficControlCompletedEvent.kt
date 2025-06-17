package com.kona.ktc.domain.event

import com.kona.common.infrastructure.message.rabbitmq.dto.TrafficStatusMessage
import com.kona.ktc.domain.model.Traffic
import com.kona.ktc.domain.model.TrafficWaiting

data class TrafficControlCompletedEvent(
    val message: TrafficStatusMessage
) {
    constructor(traffic: Traffic, waiting: TrafficWaiting) : this(
        message = TrafficStatusMessage(
            zoneId = traffic.zoneId,
            token = traffic.token,
            clientIP = traffic.clientIP,
            clientAgent = traffic.clientAgent,
            waitingNumber = waiting.number,
            estimatedTime = waiting.estimatedTime,
            totalCount = waiting.totalCount
        )
    )
}