package com.quadro.shared.data.messaging.events

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class TaskCreatedEvent(
    override val eventId: String = UUID.randomUUID().toString(),
    override val occurredAt: Long = System.currentTimeMillis(),
    override val version: Int = 1,
    val taskId: String,
    val projectId: String,
    val title: String,
    val description: String?,
    val status: String,
    val priority: String,
    val type: String,
    val assigneeId: String?,
) : DomainEvent

@Serializable
data class TaskUpdatedEvent(
    override val eventId: String = UUID.randomUUID().toString(),
    override val occurredAt: Long = System.currentTimeMillis(),
    override val version: Int = 1,
    val taskId: String,
    val projectId: String,
    val title: String,
    val description: String?,
    val status: String,
    val priority: String,
    val assigneeId: String?,
    val updatedAt: Long,
    val updatedBy: String
) : DomainEvent

@Serializable
data class TaskDeletedEvent(
    override val eventId: String = UUID.randomUUID().toString(),
    override val occurredAt: Long = System.currentTimeMillis(),
    override val version: Int = 1,
    val taskId: String,
    val title: String,
    val projectId: String,
) : DomainEvent

@Serializable
data class TaskAssignedEvent(
    override val eventId: String = UUID.randomUUID().toString(),
    override val occurredAt: Long = System.currentTimeMillis(),
    override val version: Int = 1,
    val taskId: String,
    val title: String,
    val projectId: String,
    val assigneeId: String,
) : DomainEvent

@Serializable
data class TaskCommentEvent(
    override val eventId: String = UUID.randomUUID().toString(),
    override val occurredAt: Long = System.currentTimeMillis(),
    override val version: Int = 1,
    val taskId: String,
    val commentId: String,
) : DomainEvent