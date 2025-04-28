package com.kona.common.application.dto

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

abstract class BaseResponse<T>(
    private val result: ResultResponse = ResultResponse()
) {

    fun getResult() = this.result

    abstract fun success(httpStatus: HttpStatus): ResponseEntity<T>

    fun ok(response: T): ResponseEntity<T> {
        return ResponseEntity(response, HttpStatus.OK)
    }

}