package com.quadro.shared.events

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.UUID
import kotlin.time.Instant

@Serializable
data class TeamCreatedEvent(
    override val eventId: String = UUID.randomUUID().toString(),
    override val occurredAt: Long = System.currentTimeMillis(),
    override val version: Int = 1,
    val teamId: String,
    val companyId: String,
    val name: String,
    val createdBy: String,
) : DomainEvent

@Serializable
data class TeamMemberAddedEvent(
    override val eventId: String = UUID.randomUUID().toString(),
    override val occurredAt: Long = System.currentTimeMillis(),
    override val version: Int = 1,
    val teamId: String,
    val companyId: String,
    val userId: String,
    val role: String,
) : DomainEvent

@Serializable
data class TeamMemberRemovedEvent(
    override val eventId: String = UUID.randomUUID().toString(),
    override val occurredAt: Long = System.currentTimeMillis(),
    override val version: Int = 1,
    val teamId: String,
    val companyId: String,
) : DomainEvent