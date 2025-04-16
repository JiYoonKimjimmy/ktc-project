package com.kona.ktca.controller

import com.kona.common.infra.exception.ErrorException
import com.kona.common.infra.exception.SampleErrorCode
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class SampleController {

    @GetMapping("/hello")
    fun hello(): Map<String, String> {
        return mapOf("message" to "Hello from KTCA module!")
    }

    @GetMapping("/error")
    fun error(): Map<String, String> {
        throw ErrorException(SampleErrorCode.PHONE_NUMBER_DUPLICATE)
    }
}
