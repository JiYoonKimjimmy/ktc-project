package com.kona.ktcw.infrastructure.websocket

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.web.reactive.socket.WebSocketMessage
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient
import org.springframework.web.reactive.socket.client.WebSocketClient
import reactor.core.publisher.Mono
import java.net.URI
import java.time.Duration
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class TrafficControlWebSocketHandlerBootTest(
    @LocalServerPort private val port: Int,
) : BehaviorSpec({

    val webSocketClient: WebSocketClient = ReactorNettyWebSocketClient()
    val url = URI("ws://localhost:$port/waiting")

    given("Client 트래픽 대기 요청하여") {
        then("Server 메시지 'ORDER', 'PONG', 'WAITING', 'SESSIONS' 모두 수신 정상 확인한다") {
            val messages = mutableListOf<String>()
            val latch = CountDownLatch(4) // ORDER, PONG, WAITING, SESSIONS

            webSocketClient.execute(url) { session ->
                val receive = session.receive()
                    .map(WebSocketMessage::getPayloadAsText)
                    .doOnNext {
                        println("수신 메시지: $it")
                        messages.add(it)
                        latch.countDown()
                    }
                    .take(Duration.ofSeconds(7)) // 7초 동안 메시지 수신

                val send = Mono.defer {
                    session
                        .send(Mono.just(session.textMessage("PING:123")))
                        .then(session.send(Mono.just(session.textMessage("UPDATE:2"))))
                        .then(Mono.delay(Duration.ofSeconds(6)).then(session.send(Mono.just(session.textMessage("LEAVE")))))
                }

                send.thenMany(receive).then()
            }.subscribe()

            latch.await(10, TimeUnit.SECONDS) shouldBe true

            messages shouldContain "ORDER:1"
            messages shouldContain "PONG:123"
            messages.any { it.startsWith("WAITING:") } shouldBe true
            messages.any { it.startsWith("SESSIONS:") } shouldBe true
        }
    }

    given("동시 트래픽 대기 요청 되어") {
        val clientCount = 3
        val latches = List(clientCount) { CountDownLatch(1) }
        val messages = List(clientCount) { mutableListOf<String>() }

        `when`("Client 트래픽 대기 요청 시도하는 경우") {
            then("각 Client 별 다른 대기 번호 메시지 수신 정상 확인한다") {
                repeat(clientCount) { index ->
                    webSocketClient.execute(url) { session ->
                        session.receive()
                            .map(WebSocketMessage::getPayloadAsText)
                            .flatMap { msg ->
                                println("수신 메시지: $msg")
                                messages[index].add(msg)
                                latches[index].countDown()
                                if (msg.startsWith("ORDER:")) {
                                    session.send(Mono.just(session.textMessage("LEAVE")))
                                } else {
                                    Mono.empty()
                                }
                            }
                            .then()
                    }.subscribe()
                }

                latches.forEach { it.await(5, TimeUnit.SECONDS) shouldBe true }

                val orders = messages.map { it.first().replace("ORDER:", "").toInt() }
                orders.toSet().size shouldBe clientCount  // 모든 순번이 서로 달라야 함
            }
        }
    }
}) 