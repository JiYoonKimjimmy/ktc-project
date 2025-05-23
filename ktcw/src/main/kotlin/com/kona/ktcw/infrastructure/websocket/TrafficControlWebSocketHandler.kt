package com.kona.ktcw.infrastructure.websocket

import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.WebSocketMessage
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.Sinks

@Component
class TrafficControlWebSocketHandler(
    private val trafficControlWebSocketService: TrafficControlWebSocketService,
) : WebSocketHandler {

    companion object {
        const val MAX_CONNECTIONS = 120_000
    }

    private final val waitingSinks: Sinks.Many<String> = Sinks.many().multicast().directBestEffort()
    private var activeSessions = 0

    override fun handle(session: WebSocketSession): Mono<Void> {
        activeSessions = trafficControlWebSocketService.getSessionCount()

        if (activeSessions > MAX_CONNECTIONS) {
            // 최대 접속 초과 시, 거절 메세지 리턴
            return session.send(Mono.just(session.textMessage("BUSY"))).then(session.close())
        }

        // 세션아이디와 웹소켓세션을 큐에 저장
        val sessionId = session.id
        val order = trafficControlWebSocketService.assignOrder(sessionId, session)

        // 최초 접속 시, 대기 순번을 알려줌
        val sendInitial = session.send(Mono.just(session.textMessage("ORDER:$order")))

        val inbound = session.receive()
            .map(WebSocketMessage::getPayloadAsText)
            .flatMap { msg ->
                when {
                    msg.startsWith("PING:") -> {
                        session.send(Mono.just(session.textMessage("PONG:${msg.replace("PING:", "")}")))
                    }
                    msg.startsWith("UPDATE:") -> {
                        val newOrder = msg.replace("UPDATE:", "").trim().toInt()
                        trafficControlWebSocketService.updateOrder(sessionId, newOrder)
                        Mono.empty()
                    }
                    msg.startsWith("LEAVE") -> {
                        val leftOrder = trafficControlWebSocketService.removeSession(sessionId)
                        if (leftOrder != null) {
                            waitingSinks.tryEmitNext("WAITING:$leftOrder")
                            session.close()
                        } else {
                            Mono.empty()
                        }
                    }
                    else -> Mono.empty()
                }
            }
            .thenMany(Flux.never<Void>())

        val outbound = waitingSinks.asFlux().map(session::textMessage)

        return sendInitial.then(Mono.`when`(inbound, session.send(outbound)))
    }

    @Scheduled(fixedRate = 5_000)
    fun broadcastSessionCount() {
        activeSessions = trafficControlWebSocketService.getSessionCount()
        waitingSinks.tryEmitNext("SESSIONS:$activeSessions")
        println("총 대기자 : $activeSessions")
    }

    @Scheduled(fixedRate = 5_000)
    fun clearDeadSessions() {
        trafficControlWebSocketService.clearDeadSessions()
    }

}