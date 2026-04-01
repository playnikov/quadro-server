package com.quadro.shared.events

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
sealed class TeamEvent {
    abstract val teamId: String
    abstract val companyId: String

    @Serializable
    @SerialName("created")
    data class Created(
        override val teamId: String,
        override val companyId: String,
        val name: String,
        val description: String?,
        val createdAt: Instant
    ) : TeamEvent()

    @Serializable
    @SerialName("updated")
    data class Updated(
        override val teamId: String,
        override val companyId: String,
        val name: String?,
        val description: String?,
        val updatedAt: Instant
    ) : TeamEvent()

    @Serializable
    @SerialName("deleted")
    data class Deleted(
        override val teamId: String,
        override val companyId: String,
        val deletedAt: Instant
    ) : TeamEvent()

    @Serializable
    @SerialName("member_added")
    data class MemberAdded(
        override val teamId: String,
        override val companyId: String,
        val userId: String,
        val addedAt: Instant
    ) : TeamEvent()

    @Serializable
    @SerialName("member_removed")
    data class MemberRemoved(
        override val teamId: String,
        override val companyId: String,
        val userId: String,
        val removedAt: Instant
    ) : TeamEvent()
}