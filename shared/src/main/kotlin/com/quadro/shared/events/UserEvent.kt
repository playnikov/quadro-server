package com.quadro.shared.events

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
sealed class UserEvent {
    abstract val userId: String

    @Serializable
    @SerialName("created")
    data class Created(
        override val userId: String,
        val email: String,
        val lastName: String,
        val firstName: String,
        val middleName: String?,
        val avatar: String?,
        val role: String,
        val isActive: Boolean,
        val updatedAt: Instant
    ) : UserEvent()

    @Serializable
    @SerialName("updated")
    data class Updated(
        override val userId: String,
        val email: String,
        val lastName: String,
        val firstName: String,
        val middleName: String?,
        val avatar: String?,
        val role: String,
        val isActive: Boolean,
        val updatedAt: Instant
    ) : UserEvent()

    @Serializable
    @SerialName("deleted")
    data class Deleted(
        override val userId: String,
        val deletedAt: Instant
    ) : UserEvent()
}