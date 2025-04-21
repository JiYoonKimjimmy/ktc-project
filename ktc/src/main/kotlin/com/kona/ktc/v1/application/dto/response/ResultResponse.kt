package com.kona.ktc.v1.application.dto.response

data class ResultResponse(
    val status: String = "SUCCESS",
    val code: String? = null,
    val message: String? = null
) 