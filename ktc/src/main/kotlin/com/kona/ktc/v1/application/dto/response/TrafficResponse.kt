package com.kona.ktc.v1.application.dto.response

data class TrafficResponse(
    val canEnter: Boolean,
    val zoneId: String,
    val token: String,
    val waiting: TrafficWaitResponse? = null,
    val result: ResultResponse = ResultResponse()
) 