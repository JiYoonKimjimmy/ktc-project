package com.kona.common.application.dto

import com.kona.common.enumerate.ResultStatus

data class ResultResponse(
    val status: ResultStatus = ResultStatus.SUCCESS,
    val code: String? = null,
    val message: String? = null,
    val detailMessage: String? = null
)