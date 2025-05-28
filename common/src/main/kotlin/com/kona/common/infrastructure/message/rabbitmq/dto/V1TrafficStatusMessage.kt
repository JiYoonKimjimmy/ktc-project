package com.kona.common.infrastructure.message.rabbitmq.dto

import com.kona.common.infrastructure.message.rabbitmq.BaseMessage

data class V1TrafficStatusMessage(

    val zoneId: String,
    val token: String,
    val clientIP: String?,
    val clientAgent: String?,
    val waitingNumber: Long,
    val estimatedTime: Long,
    val totalCount: Long

) : BaseMessage()