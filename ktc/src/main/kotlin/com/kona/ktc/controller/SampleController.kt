package com.kona.ktc.controller

import com.kona.common.exception.ErrorException
import com.kona.common.exception.SampleErrorCode
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class SampleController {

    @GetMapping("/hello")
    fun hello(): Map<String, String> {
        return mapOf("message" to "Hello from KTC module!")
    }

    @GetMapping("/error")
    fun error(): Map<String, String> {
        throw ErrorException(SampleErrorCode.MEMBER_NOT_FOUND)
    }
}
