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
class KtcWebSocketHandler(
    private val ktcWebSocketService: KtcWebSocketService,
) : WebSocketHandler {

    companion object {
        const val MAX_CONNECTIONS = 120_000
    }

    private final val waitingSinks: Sinks.Many<String> = Sinks.many().multicast().directBestEffort()
    private var activeSessions = 0

    override fun handle(session: WebSocketSession): Mono<Void> {
        activeSessions = ktcWebSocketService.getSessionCount()

        if (activeSessions > MAX_CONNECTIONS) {
            // 최대 접속 초과 시, 거절 메세지 리턴
            return session.send(Mono.just(session.textMessage("BUSY"))).then(session.close())
        }

        // 세션아이디와 웹소켓세션을 큐에 저장
        val sessionId = session.id
        val order = ktcWebSocketService.assignOrder(sessionId, session)

        // 최초 접속 시, 대기 순번을 알려줌
        val sendInitial = session.send(Mono.just(session.textMessage("ORDER:$order")))

        val inbound = session.receive()
            .map(WebSocketMessage::getPayloadAsText)
            .doOnNext { msg ->
                if (msg.startsWith("PING:")) {
                    session.send(Mono.just(session.textMessage("PONG:${msg.replace("PING:", "")}"))).subscribe()
                }

                if (msg.startsWith("UPDATE:")) {
                    // 순번 계산은 클라이언트가 수행하고, 서버에 보고하면 서버의 큐에 해당 정보 갱신
                    val newOrder = msg.replace("UPDATE:", "").trim().toInt()
                    ktcWebSocketService.updateOrder(sessionId, newOrder)

                } else if (msg.startsWith("LEAVE")) {
                    // 누군가가 대기열을 이탈하면 (앱 강제종료 등) 브로드 캐스팅하여 앱이 각자의 순번을 갱신할 수 있도록 함
                    val leftOrder = ktcWebSocketService.removeSession(sessionId)
                    if (leftOrder != null) {
                        waitingSinks.tryEmitNext("WAITING:$leftOrder")
                        session.close().subscribe()
                    }
                }
            }
            // 수신 스트림을 계속 열어둠
            .thenMany(Flux.never<Void>())

        val outbound = waitingSinks.asFlux().map(session::textMessage)

        return sendInitial.then(Mono.`when`(inbound, session.send(outbound))).doFinally {  }
    }

    @Scheduled(fixedRate = 5_000)
    fun broadcastSessionCount() {
        activeSessions = ktcWebSocketService.getSessionCount()
        waitingSinks.tryEmitNext("SESSIONS:$activeSessions")
        println("총 대기자 : $activeSessions")
    }

    @Scheduled(fixedRate = 5_000)
    fun clearDeadSessions() {
        ktcWebSocketService.clearDeadSessions()
    }

}