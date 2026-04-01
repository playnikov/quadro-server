package com.quadro.shared.events

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
sealed class ProjectEvent {
    abstract val projectId: String
    abstract val companyId: String

    @Serializable
    @SerialName("created")
    data class Created(
        override val projectId: String,
        override val companyId: String,
        val name: String,
        val description: String?,
        val createdAt: Instant
    ) : ProjectEvent()

    @Serializable
    @SerialName("updated")
    data class Updated(
        override val projectId: String,
        override val companyId: String,
        val name: String,
        val description: String?,
        val updatedAt: Instant
    ) : ProjectEvent()

    @Serializable
    @SerialName("deleted")
    data class Deleted(
        override val projectId: String,
        override val companyId: String,
        val deletedAt: Instant
    ) : ProjectEvent()
}