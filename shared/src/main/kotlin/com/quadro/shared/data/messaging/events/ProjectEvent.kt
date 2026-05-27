package com.quadro.shared.data.messaging.events

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class ProjectCreatedEvent(
    override val eventId: String = UUID.randomUUID().toString(),
    override val occurredAt: Long = System.currentTimeMillis(),
    override val version: Int = 1,
    val projectId: String,
    val ownerId: String,
    val name: String,
    val key: String,
    val status: String,
    val updatedAt: Long = System.currentTimeMillis(),
) : DomainEvent

@Serializable
data class ProjectUpdatedEvent(
    override val eventId: String = UUID.randomUUID().toString(),
    override val occurredAt: Long = System.currentTimeMillis(),
    override val version: Int = 1,
    val projectId: String,
    val updateBy: String,
    val key: String,
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
    val archivedBy: String,
) : DomainEvent

@Serializable
data class ProjectDeletedEvent(
    override val eventId: String = UUID.randomUUID().toString(),
    override val occurredAt: Long = System.currentTimeMillis(),
    override val version: Int = 1,
    val projectId: String,
    val deletedBy: String,
) : DomainEvent

@Serializable
data class ProjectMemberAddedEvent(
    override val eventId: String = UUID.randomUUID().toString(),
    override val occurredAt: Long = System.currentTimeMillis(),
    override val version: Int = 1,
    val projectId: String,
    val userId: String,
    val role: String
) : DomainEvent

@Serializable
data class ProjectMemberRemovedEvent(
    override val eventId: String = UUID.randomUUID().toString(),
    override val occurredAt: Long = System.currentTimeMillis(),
    override val version: Int = 1,
    val projectId: String,
    val userId: String
) : DomainEvent

@Serializable
data class ProjectMemberUpdatedRoleEvent(
    override val eventId: String = UUID.randomUUID().toString(),
    override val occurredAt: Long = System.currentTimeMillis(),
    override val version: Int = 1,
    val projectId: String,
    val userId: String,
    val role: String
) : DomainEvent

@Serializable
data class ProjectInviteCreateEvent(
    override val eventId: String = UUID.randomUUID().toString(),
    override val occurredAt: Long = System.currentTimeMillis(),
    override val version: Int = 1,
    val projectName: String,
    val userId: String? = null
) : DomainEvent