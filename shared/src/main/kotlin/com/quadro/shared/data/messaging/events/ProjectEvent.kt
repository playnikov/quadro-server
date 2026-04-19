package com.quadro.shared.data.messaging.events

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class ProjectCreatedEvent(
    override val eventId: String = UUID.randomUUID().toString(),
    override val occurredAt: Long = System.currentTimeMillis(),
    override val version: Int = 1,
    val projectId: String,
    val companyId: String,
    val name: String,
    val status: String,
    val updatedAt: Long = System.currentTimeMillis(),
) : DomainEvent

@Serializable
data class ProjectUpdatedEvent(
    override val eventId: String = UUID.randomUUID().toString(),
    override val occurredAt: Long = System.currentTimeMillis(),
    override val version: Int = 1,
    val projectId: String,
    val companyId: String,
    val name: String,
    val status: String,
    val updatedAt: Long = System.currentTimeMillis(),
) : DomainEvent

@Serializable
data class ProjectArchivedEvent(
    override val eventId: String = UUID.randomUUID().toString(),
    override val occurredAt: Long = System.currentTimeMillis(),
    override val version: Int = 1,
    val projectId: String,
    val companyId: String,
    val archivedBy: String,
) : DomainEvent

@Serializable
data class ProjectDeletedEvent(
    override val eventId: String = UUID.randomUUID().toString(),
    override val occurredAt: Long = System.currentTimeMillis(),
    override val version: Int = 1,
    val projectId: String,
    val companyId: String,
    val deletedBy: String,
) : DomainEvent