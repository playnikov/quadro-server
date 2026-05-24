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
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.channels.consumeEach
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
            val userId = call.request.headers["X-User-Id"] ?: return@webSocket

            val sessionId = UUID.randomUUID().toString()

            try {
                WebSocketManager.register(sessionId, this, userId)
                logger.info("WS connected: userId=$userId, sessionId=$sessionId")

                incoming.consumeEach { frame ->
                    if (frame is Frame.Close) return@consumeEach
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