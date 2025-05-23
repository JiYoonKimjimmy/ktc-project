package com.kona.ktcw.infrastructure.conifg

import org.springframework.boot.web.embedded.netty.NettyReactiveWebServerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import reactor.netty.http.server.HttpServer
import java.time.Duration

@Configuration
class NettyConfig {

    @Bean
    fun nettyReactiveWebServerFactory(): NettyReactiveWebServerFactory {
        val factory = NettyReactiveWebServerFactory()
        // ✅ 유휴 상태 10초 이상 → 커넥션 자동 종료
        factory.addServerCustomizers({ httpServer: HttpServer -> httpServer.idleTimeout(Duration.ofSeconds(10)) })
        return factory
    }

}