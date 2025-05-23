package com.kona.ktcw.infrastructure.conifg

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter

@Configuration
class WebSocketConfig(
    private val trafficControlWebSocketHandler: WebSocketHandler,
) {

    @Bean
    fun webSocketMapping(): SimpleUrlHandlerMapping {
        val mapping = SimpleUrlHandlerMapping()
        mapping.order = -1
        mapping.urlMap = mapOf<String, WebSocketHandler>("/waiting" to trafficControlWebSocketHandler)
        return mapping
    }

    @Bean
    fun handlerAdapter(): WebSocketHandlerAdapter {
        return WebSocketHandlerAdapter()
    }

}