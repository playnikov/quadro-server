package com.quadro.shared.events

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.UUID
import kotlin.time.Instant

@Serializable
data class UserCreatedEvent(
    override val eventId: String = UUID.randomUUID().toString(),
    override val occurredAt: Long = System.currentTimeMillis(),
    override val version: Int = 1,
    val userId: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val middleName: String?,
    val avatar: String?,
    val isActive: Boolean
) : DomainEvent

@Serializable
data class UserUpdatedEvent(
    override val eventId: String = UUID.randomUUID().toString(),
    override val occurredAt: Long = System.currentTimeMillis(),
    override val version: Int = 1,
    val userId: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val middleName: String,
    val avatar: String,
    val isActive: Boolean,
    val updatedAt: Instant
) : DomainEvent

@Serializable
data class UserDeactivatedEvent(
    override val eventId: String = UUID.randomUUID().toString(),
    override val occurredAt: Long = System.currentTimeMillis(),
    override val version: Int = 1,
    val userId: String,
) : DomainEvent