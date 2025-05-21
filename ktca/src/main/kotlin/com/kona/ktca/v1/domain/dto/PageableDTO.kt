package com.kona.ktca.v1.domain.dto

import org.springframework.data.domain.PageRequest

data class PageableDTO(
    val number: Int = 0,
    val size: Int = 20,
    val first: Boolean = false,
    val second: Boolean = false,
    val numberOfElements: Int = 0,
    val totalPage: Int = 0,
    val totalElements: Int = 0
) {

    fun toPageRequest(): PageRequest {
        return PageRequest.of(number, size)
    }

}