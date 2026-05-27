package com.quadro.notification.plugins

import com.quadro.notification.infrastructure.websocket.WebSocketManager
import com.quadro.shared.security.getUserId
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.pingPeriod
import io.ktor.server.websocket.timeout
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.CloseReason
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.channels.consumeEach
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.slf4j.LoggerFactory
import java.util.UUID
import kotlin.time.Duration.Companion.seconds

private val logger = LoggerFactory.getLogger("WebSocketPlugin")

fun Application.configureWebSocket() {
    install(WebSockets) {
        pingPeriod = 15.seconds
        timeout = 60.seconds
        maxFrameSize = Long.MAX_VALUE
    }

    routing {
        webSocket("/ws/notifications") {
            val userId = call.request.headers["X-User-Id"]
                ?: call.request.queryParameters["userId"]
                ?: return@webSocket close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "Missing userId"))

            val initialProjectIds = call.request.queryParameters["projectIds"]?.split(",")?.filter { it.isNotBlank() } ?: emptyList()

            val sessionId = UUID.randomUUID().toString()

            try {
                WebSocketManager.register(sessionId, this, userId, initialProjectIds)
                logger.info("WS connected: userId=$userId, sessionId=$sessionId, projects=$initialProjectIds")

                for (frame in incoming) {
                    when (frame) {
                        is Frame.Text -> {
                            val text = frame.readText()
                            handleIncomingCommand(sessionId, text)
                        }
                        is Frame.Close -> break
                        else -> {}
                    }
                }
            } catch (e: ClosedReceiveChannelException) {
                logger.info("WS closed by client: userId=$userId, sessionId=$sessionId")
            } catch (e: Exception) {
                logger.error("WS error: userId=$userId, sessionId=$sessionId — ${e.message}", e)
                close(CloseReason(CloseReason.Codes.INTERNAL_ERROR, "Internal error"))
            } finally {
                WebSocketManager.unregister(sessionId, userId)
                logger.info("WS session cleaned up: userId=$userId, sessionId=$sessionId")
            }
        }
    }
}

private fun handleIncomingCommand(sessionId: String, command: String) {
    val json = Json { ignoreUnknownKeys = true }
    try {
        val obj = json.parseToJsonElement(command).jsonObject
        val action = obj["action"]?.jsonPrimitive?.content
        val projectId = obj["projectId"]?.jsonPrimitive?.content
        val taskId = obj["taskId"]?.jsonPrimitive?.content

        when (action) {
            null -> {
                logger.warn("Missing action in command: $command")
                return
            }
            "subscribe" if projectId != null -> {
                WebSocketManager.subscribeToProject(sessionId, projectId)
            }
            "unsubscribe" if projectId != null -> {
                WebSocketManager.unsubscribeFromProject(sessionId, projectId)
            }
            "subscribe_task" if taskId != null -> {
                WebSocketManager.subscribeToTask(sessionId, taskId)
            }
            "unsubscribe_task" if taskId != null -> {
                WebSocketManager.unsubscribeFromTask(sessionId, taskId)
            }
            else -> {
                logger.warn("Unknown action or missing required id: action=$action, projectId=$projectId, taskId=$taskId")
            }
        }
    } catch (e: Exception) {
        logger.error("Failed to parse command: $command", e)
    }
}