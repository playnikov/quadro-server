package com.quadro.notification.infrastructure.websocket

import io.ktor.websocket.DefaultWebSocketSession
import io.ktor.websocket.send
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.slf4j.LoggerFactory
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

object WebSocketManager {
    private val logger = LoggerFactory.getLogger(WebSocketManager::class.java)

    private val sessions = ConcurrentHashMap<String, DefaultWebSocketSession>()
    private val userIndex = ConcurrentHashMap<String, MutableSet<String>>()


    fun register(sessionId: String, session: DefaultWebSocketSession, userId: String) {
        sessions[sessionId] = session
        userIndex.getOrPut(userId) { ConcurrentHashMap.newKeySet() }.add(sessionId)
        logger.info("WS registered: userId=$userId, sessionId=$sessionId, " +
                "total sessions for user=${userIndex[userId]?.size}")
    }

    fun unregister(sessionId: String, userId: String) {
        sessions.remove(sessionId)
        userIndex[userId]?.remove(sessionId)
        if (userIndex[userId].isNullOrEmpty()) userIndex.remove(userId)
        logger.info("WS unregistered: userId=$userId, sessionId=$sessionId")
    }

    suspend fun sendNotification(userId: String, message: String) {
        val sessionIds = userIndex[userId]

        if (sessionIds.isNullOrEmpty()) {
            logger.debug("User $userId is offline")
            return
        }

        val deadSessions = mutableListOf<String>()
        for (sid in sessionIds) {
            val session = sessions[sid] ?: continue
            try {
                session.send(message)
                logger.debug("Notification sent: userId=$userId, sessionId=$sid")
            } catch (e: Exception) {
                logger.warn("Failed to send to sessionId=$sid (userId=$userId): ${e.message}")
                deadSessions.add(sid)
            }
        }
        deadSessions.forEach { unregister(it, userId) }
    }

    fun getUserSession(userId: String): DefaultWebSocketSession? = sessions[userId]

    fun isOnline(userId: String): Boolean = !userIndex[userId].isNullOrEmpty()
}