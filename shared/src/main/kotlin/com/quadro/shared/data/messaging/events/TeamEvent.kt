package com.quadro.shared.data.messaging.events

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class TeamCreatedEvent(
    override val eventId: String = UUID.randomUUID().toString(),
    override val occurredAt: Long = System.currentTimeMillis(),
    override val version: Int = 1,
    val teamId: String,
    val name: String,
    val status: String,
    val createdBy: String,
) : DomainEvent

@Serializable
data class TeamUpdatedEvent(
    override val eventId: String = UUID.randomUUID().toString(),
    override val occurredAt: Long = System.currentTimeMillis(),
    override val version: Int = 1,
    val teamId: String,
    val name: String,
    val status: String,
    val updatedBy: String
) : DomainEvent

@Serializable
data class TeamDeletedEvent(
    override val eventId: String = UUID.randomUUID().toString(),
    override val occurredAt: Long = System.currentTimeMillis(),
    override val version: Int = 1,
    val teamId: String,
    val deletedBy: String
) : DomainEvent

@Serializable
data class TeamMemberAddedEvent(
    override val eventId: String = UUID.randomUUID().toString(),
    override val occurredAt: Long = System.currentTimeMillis(),
    override val version: Int = 1,
    val teamId: String,
    val userId: String,
    val role: String,
    val isActive: Boolean
) : DomainEvent

@Serializable
data class TeamMemberUpdatedEvent(
    override val eventId: String = UUID.randomUUID().toString(),
    override val occurredAt: Long = System.currentTimeMillis(),
    override val version: Int = 1,
    val teamId: String,
    val userId: String,
    val role: String,
    val isActive: Boolean
) : DomainEvent

@Serializable
data class TeamMemberRemovedEvent(
    override val eventId: String = UUID.randomUUID().toString(),
    override val occurredAt: Long = System.currentTimeMillis(),
    override val version: Int = 1,
    val teamId: String,
    val userId: String
) : DomainEvent

@Serializable
data class TeamProjectAssignedEvent(
    override val eventId: String = UUID.randomUUID().toString(),
    override val occurredAt: Long = System.currentTimeMillis(),
    override val version: Int = 1,
    val teamId: String,
    val projectId: String,
    val role: String
) : DomainEvent

@Serializable
data class TeamProjectUpdatedEvent(
    override val eventId: String = UUID.randomUUID().toString(),
    override val occurredAt: Long = System.currentTimeMillis(),
    override val version: Int = 1,
    val teamId: String,
    val projectId: String,
    val role: String
) : DomainEvent

@Serializable
data class TeamProjectUnassignedEvent(
    override val eventId: String = UUID.randomUUID().toString(),
    override val occurredAt: Long = System.currentTimeMillis(),
    override val version: Int = 1,
    val teamId: String,
    val projectId: String,
) : DomainEvent