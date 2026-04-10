package com.quadro.shared.events

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.UUID
import kotlin.time.Instant

@Serializable
data class TaskCreatedEvent(
    override val eventId: String = UUID.randomUUID().toString(),
    override val occurredAt: Long = System.currentTimeMillis(),
    override val version: Int = 1,
    val taskId: String,
    val projectId: String,
    val companyId: String,
    val title: String,
    val type: String,
    val priority: String,
    val status: String,
    val createdBy: String,
    val assigneeId: String?,
) : DomainEvent

@Serializable
data class TaskUpdatedEvent(
    override val eventId: String = UUID.randomUUID().toString(),
    override val occurredAt: Long = System.currentTimeMillis(),
    override val version: Int = 1,
    val taskId: String,
    val projectId: String,
    val companyId: String,
    val updatedBy: String,
    val changes: Map<String, String>,
) : DomainEvent

@Serializable
data class TaskAssignedEvent(
    override val eventId: String = UUID.randomUUID().toString(),
    override val occurredAt: Long = System.currentTimeMillis(),
    override val version: Int = 1,
    val taskId: String,
    val projectId: String,
    val companyId: String,
    val assigneeId: String,
    val assignedBy: String,
) : DomainEvent

@Serializable
data class TaskStatusChangedEvent(
    override val eventId: String = UUID.randomUUID().toString(),
    override val occurredAt: Long = System.currentTimeMillis(),
    override val version: Int = 1,
    val taskId: String,
    val projectId: String,
    val companyId: String,
    val oldStatus: String,
    val newStatus: String,
    val changedBy: String,
) : DomainEvent

@Serializable
data class TaskCompletedEvent(
    override val eventId: String = UUID.randomUUID().toString(),
    override val occurredAt: Long = System.currentTimeMillis(),
    override val version: Int = 1,
    val taskId: String,
    val projectId: String,
    val companyId: String,
    val completedBy: String,
    val completedAt: Long,
) : DomainEvent

@Serializable
data class TaskDeletedEvent(
    override val eventId: String = UUID.randomUUID().toString(),
    override val occurredAt: Long = System.currentTimeMillis(),
    override val version: Int = 1,
    val taskId: String,
    val projectId: String,
    val companyId: String,
) : DomainEvent