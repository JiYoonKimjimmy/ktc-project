package com.kona.ktc.v0.presentation.dto

import com.kona.ktc.v0.presentation.dto.common.ResultDto

data class TrafficWaitResponseDto(
    val canEnter: Boolean,
    val zoneId: String,
    val token: String,
    val waiting: TrafficWaitingDto? = null,
    val result: ResultDto
)



