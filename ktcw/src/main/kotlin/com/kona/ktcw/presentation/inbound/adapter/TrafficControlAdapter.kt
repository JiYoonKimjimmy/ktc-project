package com.kona.ktcw.presentation.inbound.adapter

import org.springframework.core.io.ClassPathResource
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
class TrafficControlAdapter {

    @GetMapping("/waiting-room", produces = [MediaType.TEXT_HTML_VALUE])
    fun waitingRoom(): Mono<ResponseEntity<ClassPathResource>> {
        val html = ClassPathResource("static/netty_webflux_websocket_client1.html")
        return Mono.just(ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(html))
    }

}