package com.quadro.shared.events

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
sealed class CompanyEvent {
    abstract val companyId: String

    @Serializable
    @SerialName("created")
    data class Created(
        override val companyId: String,
        val name: String,
        val status: String,
        val currentProjects: Int,
        val maxProjects: Int,
        val updatedAt: Instant
    ) : CompanyEvent()

    @Serializable
    @SerialName("updated")
    data class Updated(
        override val companyId: String,
        val name: String,
        val status: String,
        val currentProjects: Int,
        val maxProjects: Int,
        val updatedAt: Instant
    ) : CompanyEvent()

    @Serializable
    @SerialName("deleted")
    data class Deleted(
        override val companyId: String,
        val deletedAt: Instant
    ) : CompanyEvent()

    @Serializable
    @SerialName("user_added")
    data class UserAdded(
        override val companyId: String,
        val userId: String,
        val role: String,
        val addedAt: Instant
    ) : CompanyEvent()

    @Serializable
    @SerialName("user_removed")
    data class UserRemoved(
        override val companyId: String,
        val userId: String,
        val removedAt: Instant
    ) : CompanyEvent()
}