package com.kona.common.infrastructure.config

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class CoroutineConfig {
    // logger
    private val logger = LoggerFactory.getLogger(this::class.java)

    @Bean
    fun defaultCoroutineScope(): CoroutineScope {
        val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
            logger.error("Exception in Default coroutine scope", throwable)
        }
        return CoroutineScope(Dispatchers.Default + SupervisorJob() + exceptionHandler)
    }

}