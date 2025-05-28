package com.kona.ktc.infrastructure.event

import com.kona.common.infrastructure.message.rabbitmq.dto.V1TrafficStatusMessage
import com.kona.ktc.domain.model.Traffic
import com.kona.ktc.domain.model.TrafficWaiting

data class SaveTrafficStatusEvent(
    val message: V1TrafficStatusMessage
) {
    constructor(traffic: Traffic, waiting: TrafficWaiting) : this(
        message = V1TrafficStatusMessage(
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