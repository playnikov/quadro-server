package com.quadro.shared.events

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
sealed class TaskEvent {
    abstract val taskId: String
    abstract val projectId: String
    abstract val companyId: String

    @Serializable
    @SerialName("created")
    data class Created(
        override val taskId: String,
        override val projectId: String,
        override val companyId: String,
        val title: String,
        val description: String?,
        val assignedTo: String?,
        val status: String,
        val priority: String,
        val dueDate: Instant?,
        val createdAt: Instant
    ) : TaskEvent()

    @Serializable
    @SerialName("updated")
    data class Updated(
        override val taskId: String,
        override val projectId: String,
        override val companyId: String,
        val title: String?,
        val description: String?,
        val assignedTo: String?,
        val status: String?,
        val priority: String?,
        val dueDate: Instant?,
        val updatedAt: Instant
    ) : TaskEvent()

    @Serializable
    @SerialName("deleted")
    data class Deleted(
        override val taskId: String,
        override val projectId: String,
        override val companyId: String,
        val deletedAt: Instant
    ) : TaskEvent()

    @Serializable
    @SerialName("assigned")
    data class Assigned(
        override val taskId: String,
        override val projectId: String,
        override val companyId: String,
        val assignedTo: String,
        val assignedAt: Instant
    ) : TaskEvent()

    @Serializable
    @SerialName("status_changed")
    data class StatusChanged(
        override val taskId: String,
        override val projectId: String,
        override val companyId: String,
        val oldStatus: String,
        val newStatus: String,
        val changedAt: Instant
    ) : TaskEvent()
}