package com.kona.ktc.application.usecase

import com.kona.ktc.domain.model.Traffic
import com.kona.ktc.presentation.dto.mapper.TrafficMapper
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.io.IOException
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import kotlin.system.measureTimeMillis

@Component
class TrafficControlStreamUseCase(
    private val trafficControlUseCase: TrafficControlUseCase
) {
    // logger
    private val logger = LoggerFactory.getLogger(this::class.java)

    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val emitters = ConcurrentHashMap<String, Pair<Traffic, SseEmitter>>()

    private lateinit var mapper: TrafficMapper

    suspend fun controlTraffic(traffic: Traffic, trafficMapper: TrafficMapper, now: Instant = Instant.now()): SseEmitter {
        mapper = trafficMapper
        val token = traffic.token
        val emitter = SseEmitter(Long.MAX_VALUE)

        emitter.onCompletion {
            try {
                removeEmitter(token)
                logger.info("SSE connection completed for client $token")
            } catch (e: Exception) {
                logger.error("Error during completion for client $token: ${e.javaClass.simpleName}")
            }
        }

        emitter.onTimeout {
            try {
                removeEmitter(token)
                logger.info("SSE connection timed out for client $token")
            } catch (e: Exception) {
                logger.error("Error during timeout handling for client $token: ${e.javaClass.simpleName}")
            }
        }

        emitter.onError { e ->
            try {
                removeEmitter(token)
                logger.info("SSE connection error for client $token: ${e.javaClass.simpleName}")
            } catch (ex: Exception) {
                logger.info("Error during error handling for client $token: ${ex.javaClass.simpleName}")
            }
        }

        emitters[token] = Pair(traffic, emitter)

        try {
            val result = traffic.checkWaiting(now).let(mapper::toResponse)

            if (result.canEnter) {
                removeEmitter(token)
            } else {
                SseEmitter.event()
                    .id(token)
                    .name("connect")
                    .data(result, MediaType.APPLICATION_JSON)
                    .let { emitter.send(it) }
            }

        } catch (e: IOException) {
            removeEmitter(token)
            logger.error("Error sending initial SSE event to client $token", e)
        }

        return emitter
    }

    @Scheduled(fixedRate = 1000)
    suspend fun broadcastTrafficUpdate() {
        if (emitters.isNotEmpty()) {
            val measureTime = measureTimeMillis {
                // 코루틴 작업 목록 생성
                val jobs = emitters.map { (token, value) ->
                    val traffic = value.first
                    val emitter = value.second

                    coroutineScope.async(Dispatchers.IO) {
                        try {
                            val result = traffic.checkWaiting().let(this@TrafficControlStreamUseCase.mapper::toResponse)
                            if (result.canEnter) {
                                removeEmitter(token)
                            } else {
                                SseEmitter.event()
                                    .id(token)
                                    .name("waiting")
                                    .data(result, MediaType.APPLICATION_JSON)
                                    .let { emitter.send(it) }
                            }
                            // 성공 시 null 반환 (제거할 필요 없음)
                            null
                        } catch (e: Exception) {
                            logger.error("Client {} disconnected: {}", token, e.javaClass.simpleName)
                            // 실패 시 해당 ID 반환 (제거 대상)
                            token
                        }
                    }
                }

                val idsToRemove = jobs.awaitAll().filterNotNull()

                // 실패한 emitter 제거
                idsToRemove.forEach { emitters.remove(it) }
            }

            logger.warn("Traffic update broadcast execution time: ${measureTime}ms with ${emitters.size} emitter(s)")
        }
    }

    private suspend fun Traffic.checkWaiting(now: Instant = Instant.now()): Traffic {
        val waiting = trafficControlUseCase.controlTraffic(this, now)
        return this.applyWaiting(waiting)
    }

    private fun removeEmitter(token: String) {
        try {
            val emitter = emitters.remove(token)?.second
            if (emitter != null) {
                try {
                    emitter.complete()
                    logger.info("Emitter completed for client $token")
                } catch (e: Exception) {
                    logger.error("Cannot complete emitter for client $token: ${e.javaClass.simpleName}")
                }
            }
        } catch (e: Exception) {
            logger.error("Error removing emitter for client $token: ${e.javaClass.simpleName}")
        }
    }

}