package com.kona.ktc.infra.config

import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@ComponentScan(basePackages = ["com.kona.ktc", "com.kona.common"])
@Configuration
class KtcApplicationConfig : WebMvcConfigurer {
    
    override fun addViewControllers(registry: ViewControllerRegistry) {
        // favicon.ico 204 No Content 응답 반환
        registry.addStatusController("/favicon.ico", HttpStatus.NO_CONTENT)
    }

}