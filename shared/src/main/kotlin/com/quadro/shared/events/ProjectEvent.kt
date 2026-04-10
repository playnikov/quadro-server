package com.quadro.shared.events

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.UUID
import kotlin.time.Instant

@Serializable
data class ProjectCreatedEvent(
    override val eventId: String = UUID.randomUUID().toString(),
    override val occurredAt: Long = System.currentTimeMillis(),
    override val version: Int = 1,
    val projectId: String,
    val companyId: String,
    val name: String,
    val key: String,
    val ownerId: String,
    val leadId: String,
    val type: String,
    val visibility: String,
) : DomainEvent

@Serializable
data class ProjectUpdatedEvent(
    override val eventId: String = UUID.randomUUID().toString(),
    override val occurredAt: Long = System.currentTimeMillis(),
    override val version: Int = 1,
    val projectId: String,
    val companyId: String,
    val updatedBy: String,
    val changes: Map<String, String>,
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

@Serializable
data class ProjectMemberAddedEvent(
    override val eventId: String = UUID.randomUUID().toString(),
    override val occurredAt: Long = System.currentTimeMillis(),
    override val version: Int = 1,
    val projectId: String,
    val companyId: String,
    val userId: String,
    val role: String,
) : DomainEvent

@Serializable
data class ProjectTeamAssignedEvent(
    override val eventId: String = UUID.randomUUID().toString(),
    override val occurredAt: Long = System.currentTimeMillis(),
    override val version: Int = 1,
    val projectId: String,
    val companyId: String,
    val teamId: String,
    val role: String,
) : DomainEvent