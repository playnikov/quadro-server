package com.quadro.auth.domain.services

import com.quadro.auth.domain.models.User
import com.quadro.shared.events.UserEvent
import kotlinx.serialization.json.Json
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import kotlin.time.Instant

class EventPublisherImpl(
    private val producer: KafkaProducer<String, String>
) : EventPublisher {
    private val json = Json { ignoreUnknownKeys = true }

    override fun publishUserCreated(user: User) {
        val event  = UserEvent.Created(
            userId = user.id.toString(),
            email = user.email,
            lastName = user.lastName,
            firstName = user.firstName,
            middleName = user.middleName,
            avatar = user.avatarUrl,
            role = user.role.name,
            isActive = user.isActive,
            updatedAt = user.updatedAt
        )
        sendEvent(user.id.toString(), event)
    }

    override fun publishUserUpdated(user: User) {
        val event  = UserEvent.Updated(
            userId = user.id.toString(),
            email = user.email,
            lastName = user.lastName,
            firstName = user.firstName,
            middleName = user.middleName,
            avatar = user.avatarUrl,
            role = user.role.name,
            isActive = user.isActive,
            updatedAt = user.updatedAt
        )
        sendEvent(user.id.toString(), event)
    }

    override fun publishUserDeleted(userId: String, deletedAt: Instant) {
        val event  = UserEvent.Deleted(userId = userId, deletedAt = deletedAt)
        sendEvent(userId, event)
    }

    private fun sendEvent(userId: String, event: UserEvent) {
        val record = ProducerRecord("user-events", userId, json.encodeToString(event))
        producer.send(record)
    }
}