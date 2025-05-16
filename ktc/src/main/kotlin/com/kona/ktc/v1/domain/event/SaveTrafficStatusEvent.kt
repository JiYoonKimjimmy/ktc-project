package com.kona.ktc.v1.domain.event

import com.kona.common.infrastructure.message.rabbitmq.dto.V1TrafficStatusMessage
import com.kona.ktc.v1.domain.model.Traffic
import com.kona.ktc.v1.domain.model.TrafficWaiting

data class SaveTrafficStatusEvent(
    val message: V1TrafficStatusMessage
) {
    constructor(token: Traffic, waiting: TrafficWaiting) : this(
        message = V1TrafficStatusMessage(
            zoneId = token.zoneId,
            token = token.token,
            clientIp = token.clientIp,
            clientAgent = token.clientAgent?.name,
            waitingNumber = waiting.number,
            estimatedTime = waiting.estimatedTime,
            totalCount = waiting.totalCount
        )
    )
}