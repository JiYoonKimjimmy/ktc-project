package com.kona.ktcw.infrastructure.conifg

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter

@Configuration
class WebSocketConfig(
    private val ktcWebSocketHandler: WebSocketHandler,
) {

    @Bean
    fun webSocketMapping(): SimpleUrlHandlerMapping {
        val mapping = SimpleUrlHandlerMapping()
        mapping.order = -1 // 필터보다 먼저 실행
        mapping.urlMap = mapOf<String, Any?>("/waiting" to ktcWebSocketHandler) // ws 엔드포인트 등록
        return mapping
    }

    @Bean
    fun handlerAdapter(): WebSocketHandlerAdapter {
        return WebSocketHandlerAdapter()
    }

}