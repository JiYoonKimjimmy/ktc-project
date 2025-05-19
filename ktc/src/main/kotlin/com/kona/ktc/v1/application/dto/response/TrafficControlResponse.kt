package com.kona.ktc.v1.application.dto.response

import com.kona.common.application.dto.BaseResponse
import com.kona.ktc.v1.domain.model.TrafficWaiting
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

data class TrafficControlResponse(
    val canEnter: Boolean,
    val zoneId: String,
    val token: String,
    val waiting: TrafficWaitingResponse? = null
) : BaseResponse<TrafficControlResponse>() {

    data class TrafficWaitingResponse(
        val number: Long,
        val estimatedTime: Long,
        val totalCount: Long,
        val pollingPeriod: Long
    ) {
        constructor(waiting: TrafficWaiting): this(
            number = waiting.number,
            estimatedTime = waiting.estimatedTime,
            totalCount = waiting.totalCount,
            pollingPeriod = waiting.pollingPeriod
        )
    }

    override fun success(httpStatus: HttpStatus): ResponseEntity<TrafficControlResponse> {
        return ok(this)
    }

}