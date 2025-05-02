package com.kona.common.infrastructure.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.http.HttpStatus
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class CommonApplicationConfig : WebMvcConfigurer {

    override fun addViewControllers(registry: ViewControllerRegistry) {
        // favicon.ico 204 No Content 응답 반환
        registry.addStatusController("/favicon.ico", HttpStatus.NO_CONTENT)
    }

    @Primary
    @Bean
    fun objectMapper(): ObjectMapper {
        return jacksonObjectMapper().registerModules(kotlinModule(), JavaTimeModule())
    }

}