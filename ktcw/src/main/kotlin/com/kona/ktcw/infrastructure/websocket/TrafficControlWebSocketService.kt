package com.kona.ktcw.infrastructure.websocket

import org.springframework.stereotype.Service
import org.springframework.web.reactive.socket.WebSocketSession
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

@Service
class TrafficControlWebSocketService {

    private final val connectionCount = AtomicInteger(0)
    private final val sessionOrderMap = ConcurrentHashMap<String, Int>()
    private final val sessionMap = ConcurrentHashMap<String, WebSocketSession>()

    fun assignOrder(sessionId: String, session: WebSocketSession): Int {
        sessionMap[sessionId] = session
        sessionOrderMap[sessionId] = sessionMap.size
        return sessionMap.size
    }

    fun updateOrder(sessionId: String, newOrder: Int) {
        sessionOrderMap[sessionId] = newOrder
    }

    fun removeSession(sessionId: String): Int? {
        connectionCount.decrementAndGet()
        sessionMap.remove(sessionId)
        return sessionOrderMap.remove(sessionId)
    }

    fun getSessionCount(): Int {
        return sessionMap.size
    }

    fun clearDeadSessions() {
        sessionMap.forEach { sessionId, session ->
            if (!session.isOpen) {
                removeSession(sessionId)
            }
        }
        println(" 세션 수 : ${sessionMap.size}")
    }

}