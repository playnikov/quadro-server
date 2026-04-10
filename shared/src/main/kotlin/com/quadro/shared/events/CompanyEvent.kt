package com.quadro.shared.events

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.UUID
import kotlin.time.Instant

@Serializable
data class CompanyCreatedEvent(
    override val eventId: String = UUID.randomUUID().toString(),
    override val occurredAt: Long = System.currentTimeMillis(),
    override val version: Int = 1,
    val companyId: String,
    val name: String,
    val ownerId: String,
    val maxProjects: Int,
    val maxMembers: Int
) : DomainEvent

@Serializable
data class CompanyUpdatedEvent(
    override val eventId: String = UUID.randomUUID().toString(),
    override val occurredAt: Long = System.currentTimeMillis(),
    override val version: Int = 1,
    val companyId: String,
    val name: String?,
    val ownerId: String?,
    val maxProjects: Int?,
    val maxMembers: Int?,
) : DomainEvent

@Serializable
data class CompanyDeletedEvent(
    override val eventId: String = UUID.randomUUID().toString(),
    override val occurredAt: Long = System.currentTimeMillis(),
    override val version: Int = 1,
    val companyId: String
) : DomainEvent

@Serializable
data class CompanyMemberAddedEvent(
    override val eventId: String = UUID.randomUUID().toString(),
    override val occurredAt: Long = System.currentTimeMillis(),
    override val version: Int = 1,
    val companyId: String,
    val userId: String,
    val role: String,
    val invitedBy: String,
) : DomainEvent

@Serializable
data class CompanyMemberRemovedEvent(
    override val eventId: String = UUID.randomUUID().toString(),
    override val occurredAt: Long = System.currentTimeMillis(),
    override val version: Int = 1,
    val companyId: String,
    val userId: String,
) : DomainEvent

@Serializable
data class CompanyMemberRoleUpdatedEvent(
    override val eventId: String = UUID.randomUUID().toString(),
    override val occurredAt: Long = System.currentTimeMillis(),
    override val version: Int = 1,
    val companyId: String,
    val userId: String,
    val role: String,
) : DomainEvent