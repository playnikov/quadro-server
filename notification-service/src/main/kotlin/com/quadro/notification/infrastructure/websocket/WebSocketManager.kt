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
    private val sessionToUser = ConcurrentHashMap<String, String>()

    private val projectSubscriptions = ConcurrentHashMap<String, MutableSet<String>>()
    private val sessionProjects = ConcurrentHashMap<String, MutableSet<String>>()

    private val taskSubscriptions = ConcurrentHashMap<String, MutableSet<String>>()
    private val sessionTasks = ConcurrentHashMap<String, MutableSet<String>>()

    private fun checkSession(sessionId: String): DefaultWebSocketSession? {
        val session = sessions[sessionId] ?: run {
            logger.warn("Cannot subscribe: session $sessionId not found")
            return null
        }

        return session
    }

    fun register(
        sessionId: String,
        session: DefaultWebSocketSession,
        userId: String,
        projectIds: List<String> = emptyList()
    ) {
        sessions[sessionId] = session
        sessionToUser[sessionId] = userId
        userIndex.getOrPut(userId) { ConcurrentHashMap.newKeySet() }.add(sessionId)

        if (projectIds.isNotEmpty()) {
            val projSet = sessionProjects.getOrPut(sessionId) { ConcurrentHashMap.newKeySet() }
            projectIds.forEach { projectId ->
                projSet.add(projectId)
                projectSubscriptions.getOrPut(projectId) { ConcurrentHashMap.newKeySet() }.add(sessionId)
            }
        }

        logger.info("WS registered: userId=$userId, sessionId=$sessionId, projects=$projectIds, " +
                "total sessions for user=${userIndex[userId]?.size}")
    }

    fun subscribeToProject(sessionId: String, projectId: String) {
        checkSession(sessionId) ?: return

        val projSet = sessionProjects.getOrPut(sessionId) { ConcurrentHashMap.newKeySet() }
        if (projSet.add(projectId)) {
            projectSubscriptions.getOrPut(projectId) { ConcurrentHashMap.newKeySet() }.add(sessionId)
            logger.debug("Session $sessionId subscribed to project $projectId")
        }
    }

    fun unsubscribeFromProject(sessionId: String, projectId: String) {
        sessionProjects[sessionId]?.remove(projectId)
        projectSubscriptions[projectId]?.remove(sessionId)
        if (projectSubscriptions[projectId].isNullOrEmpty()) {
            projectSubscriptions.remove(projectId)
        }
        logger.debug("Session $sessionId unsubscribed from project $projectId")
    }

    fun subscribeToTask(sessionId: String, taskId: String) {
        checkSession(sessionId) ?: return

        val taskSet = sessionTasks.getOrPut(sessionId) { ConcurrentHashMap.newKeySet() }
        if (taskSet.add(taskId)) {
            taskSubscriptions.getOrPut(taskId) { ConcurrentHashMap.newKeySet() }.add(sessionId)
            logger.debug("Session $sessionId subscribed to task $taskId")
        }
    }

    fun unsubscribeFromTask(sessionId: String, taskId: String) {
        sessionTasks[sessionId]?.remove(taskId)
        taskSubscriptions[taskId]?.remove(sessionId)
        if (taskSubscriptions[taskId].isNullOrEmpty()) {
            taskSubscriptions.remove(taskId)
        }
        logger.debug("Session $sessionId unsubscribed from task $taskId")
    }

    fun unregister(sessionId: String, userId: String) {
        sessions.remove(sessionId)
        sessionToUser.remove(sessionId)
        userIndex[userId]?.remove(sessionId)
        if (userIndex[userId].isNullOrEmpty()) userIndex.remove(userId)

        val projects = sessionProjects.remove(sessionId)
        if (projects != null) {
            for (projectId in projects) {
                projectSubscriptions[projectId]?.remove(sessionId)
                if (projectSubscriptions[projectId].isNullOrEmpty()) {
                    projectSubscriptions.remove(projectId)
                }
            }
        }

        val tasks = sessionTasks.remove(sessionId)
        if (tasks != null) {
            for (taskId in tasks) {
                taskSubscriptions[taskId]?.remove(sessionId)
                if (taskSubscriptions[taskId].isNullOrEmpty()) {
                    taskSubscriptions.remove(taskId)
                }
            }
        }

        logger.info("WS unregistered: userId=$userId, sessionId=$sessionId")
    }

    suspend fun sendNotification(userId: String, message: String) {
        val sessionIds = userIndex[userId]
        if (sessionIds.isNullOrEmpty()) {
            logger.debug("User $userId is offline")
            return
        }
        sendToSessions(sessionIds, message, "userId=$userId")
    }

    suspend fun sendProjectNotification(projectId: String, message: String) {
        val sessionIds = projectSubscriptions[projectId]
        if (sessionIds.isNullOrEmpty()) {
            logger.debug("No subscribers for project $projectId")
            return
        }
        sendToSessions(sessionIds, message, "projectId=$projectId")
    }

    suspend fun sendTaskNotification(taskId: String, message: String) {
        val sessionIds = taskSubscriptions[taskId]
        if (sessionIds.isNullOrEmpty()) {
            logger.debug("No subscribers for task $taskId")
            return
        }
        sendToSessions(sessionIds, message, "taskId=$taskId")
    }

    private suspend fun sendToSessions(sessionIds: MutableSet<String>, message: String, context: String) {
        val deadSessions = mutableListOf<String>()
        for (sid in sessionIds) {
            val session = sessions[sid] ?: continue
            try {
                session.send(message)
                logger.debug("Notification sent: $context, sessionId=$sid")
            } catch (e: Exception) {
                logger.warn("Failed to send to sessionId=$sid ($context): ${e.message}")
                deadSessions.add(sid)
            }
        }
        deadSessions.forEach { sid ->
            val userId = sessionToUser[sid]
            if (userId != null) {
                unregister(sid, userId)
            } else {
                sessions.remove(sid)
                projectSubscriptions.values.forEach { it.remove(sid) }
                taskSubscriptions.values.forEach { it.remove(sid) }
                sessionProjects.remove(sid)
                logger.warn("Forced removal of dead session $sid without userId")
            }
        }
    }

    fun getUserSession(userId: String): DefaultWebSocketSession? = sessions[userId]

    fun isOnline(userId: String): Boolean = !userIndex[userId].isNullOrEmpty()
}