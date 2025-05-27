package com.kona.ktc.presentation.adapter

import com.kona.ktc.domain.port.inbound.TrafficMonitorPort
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.io.IOException
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@RequestMapping("/api/v1/traffic/sse")
@RestController
class TrafficSseAdapter(
    private val trafficMonitorPort: TrafficMonitorPort
) {
    private val logger = LoggerFactory.getLogger(TrafficSseAdapter::class.java)
    private val emitters = ConcurrentHashMap<String, SseEmitter>()
    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    /**
     * Establishes an SSE connection for monitoring traffic updates
     * @param clientId Optional client identifier
     * @return SseEmitter for streaming events to the client
     */
    @GetMapping("/connect", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    suspend fun connect(@RequestParam(required = false) clientId: String?): SseEmitter {
        val id = clientId ?: UUID.randomUUID().toString()
        val emitter = SseEmitter(Long.MAX_VALUE)

        // Add completion and error callbacks
        emitter.onCompletion {
            try {
                removeEmitter(id)
                logger.debug("SSE connection completed for client {}", id)
            } catch (e: Exception) {
                logger.debug("Error during completion for client {}: {}", id, e.javaClass.simpleName)
            }
        }

        emitter.onTimeout {
            try {
                removeEmitter(id)
                logger.debug("SSE connection timed out for client {}", id)
            } catch (e: Exception) {
                logger.debug("Error during timeout handling for client {}: {}", id, e.javaClass.simpleName)
            }
        }

        emitter.onError { e ->
            try {
                removeEmitter(id)
                logger.debug("SSE connection error for client {}: {}", id, e.javaClass.simpleName)
            } catch (ex: Exception) {
                logger.debug("Error during error handling for client {}: {}", id, ex.javaClass.simpleName)
            }
        }

        // Store emitter with client ID
        emitters[id] = emitter

        // Send initial connection event
        try {
            emitter.send(
                SseEmitter.event()
                    .id(id)
                    .name("connect")
                    .data("Connected successfully with ID: $id", MediaType.APPLICATION_JSON)
            )

            // Register client with the monitoring service
            trafficMonitorPort.registerClient(id)

        } catch (e: IOException) {
            logger.error("Error sending initial SSE event to client $id", e)
            removeEmitter(id)
        }

        return emitter
    }

    @Scheduled(fixedRate = 1000)
    suspend fun broadcastTrafficUpdate() {
        if (emitters.isNotEmpty()) {
            val startTime = System.currentTimeMillis()

            // 코루틴 작업 목록 생성
            val jobs = emitters.map { (id, emitter) ->
                // 각 emitter마다 별도 코루틴으로 처리
                coroutineScope.async(IO) {
                    try {
                        emitter.send(
                            SseEmitter.event()
                                .id(UUID.randomUUID().toString())
                                .name("event-name")
                                .data("test-data", MediaType.TEXT_PLAIN)
                        )
                        // 성공 시 null 반환 (제거할 필요 없음)
                        null
                    } catch (e: Exception) {
                        logger.debug("Client {} disconnected: {}", id, e.javaClass.simpleName)
                        // 실패 시 해당 ID 반환 (제거 대상)
                        id
                    }
                }
            }

            // 모든 작업 완료 대기 및 결과 수집
            val idsToRemove = jobs.awaitAll().filterNotNull()

            // 실패한 emitter 제거
            idsToRemove.forEach { id ->
                emitters.remove(id)
            }

            val endTime = System.currentTimeMillis()
            logger.warn("Traffic update broadcast execution time: {} ms with {} emitter(s)", endTime - startTime, emitters.size)
        }
    }

    /**
     * Manually disconnect a client from SSE updates
     * @param clientId The ID of the client to disconnect
     * @return Confirmation message
     */
    @DeleteMapping("/disconnect/{clientId}")
    suspend fun disconnect(@PathVariable clientId: String): Map<String, String> {
        removeEmitter(clientId)
        trafficMonitorPort.unregisterClient(clientId)
        return mapOf("status" to "Disconnected", "clientId" to clientId)
    }

    /**
     * Get count of currently connected clients
     * @return Count of connected clients
     */
    @GetMapping("/stats")
    fun getStats(): Map<String, Any> {
        return mapOf(
            "connectedClients" to emitters.size,
            "clientIds" to emitters.keys()
        )
    }

    private fun removeEmitter(clientId: String) {
        try {
            val emitter = emitters.remove(clientId)
            if (emitter != null) {
                try {
                    emitter.complete()
                    logger.debug("Emitter completed for client {}", clientId)
                } catch (e: Exception) {
                    logger.debug("Cannot complete emitter for client {}: {}", clientId, e.javaClass.simpleName)
                }
            }
        } catch (e: Exception) {
            logger.debug("Error removing emitter for client {}: {}", clientId, e.javaClass.simpleName)
        }
    }
}
