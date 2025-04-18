package com.kona.ktc.v0.presentation.dto.common

data class ResultDto(
    val status: String,
    val code: String? = null,
    val message: String? = null
) 