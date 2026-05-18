package com.quadro.notification.domain.services

import com.quadro.shared.data.messaging.events.DomainEvent
import io.ktor.websocket.DefaultWebSocketSession
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.concurrent.ConcurrentHashMap

class NotificationService {
    private val sessions = ConcurrentHashMap<String, SendChannel<Frame>>()
    private val eventFlow = MutableSharedFlow<DomainEvent>(replay = 0)

    val events = eventFlow.asSharedFlow()

    fun addSession(session: DefaultWebSocketSession) {
        val sessionId = session.hashCode().toString()
        sessions[sessionId] = session.outgoing

        // Запускаем прослушивание закрытия сессии
        runBlocking {
            launch {
                session.incoming.receiveAsFlow()
                    .onCompletion { removeSession(sessionId) }
                    .collect()
            }
        }
    }

    fun removeSession(sessionId: String) {
        sessions.remove(sessionId)
    }

    suspend fun sendNotification(event: DomainEvent) {
        val frame = Frame.Text(event.toString()) // В реальной реализации нужно сериализовать event
        val failedSessions = mutableListOf<String>()

        for ((sessionId, channel) in sessions) {
            try {
                channel.send(frame)
            } catch (e: Exception) {
                failedSessions.add(sessionId)
            }
        }

        // Удаляем неудачные сессии
        failedSessions.forEach { removeSession(it) }
    }
}

data class Frame(val type: FrameType, val data: String) {
    enum class FrameType {
        TEXT, BINARY
    }

    companion object {
        fun Text(data: String) = Frame(FrameType.TEXT, data)
    }
}