package com.quadro.shared.data.messaging.events

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class CompanyCreatedEvent(
    override val eventId: String = UUID.randomUUID().toString(),
    override val occurredAt: Long = System.currentTimeMillis(),
    override val version: Int = 1,
    val companyId: String,
    val name: String,
    val ownerId: String,
    val companyStatus: String,
    val createTeamRole: String,
    val maxProjects: Int,
    val maxMembers: Int,
    val updatedAt: Long = System.currentTimeMillis()
) : DomainEvent

@Serializable
data class CompanyUpdatedEvent(
    override val eventId: String = UUID.randomUUID().toString(),
    override val occurredAt: Long = System.currentTimeMillis(),
    override val version: Int = 1,
    val companyId: String,
    val name: String,
    val ownerId: String,
    val companyStatus: String,
    val createTeamRole: String,
    val maxProjects: Int,
    val maxMembers: Int,
    val updatedAt: Long = System.currentTimeMillis()
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
    val memberId: String,
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
    val memberId: String
) : DomainEvent

@Serializable
data class CompanyMemberRoleUpdatedEvent(
    override val eventId: String = UUID.randomUUID().toString(),
    override val occurredAt: Long = System.currentTimeMillis(),
    override val version: Int = 1,
    val memberId: String,
    val companyId: String,
    val userId: String,
    val role: String,
) : DomainEvent