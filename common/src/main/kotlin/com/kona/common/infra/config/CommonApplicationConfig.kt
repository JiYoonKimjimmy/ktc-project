package com.kona.common.infra.config

import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class CommonApplicationConfig : WebMvcConfigurer {

    override fun addViewControllers(registry: ViewControllerRegistry) {
        // favicon.ico 204 No Content 응답 반환
        registry.addStatusController("/favicon.ico", HttpStatus.NO_CONTENT)
    }

}