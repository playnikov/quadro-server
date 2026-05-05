package com.quadro.task.presentation.models

import com.quadro.task.domain.models.task.Sprint
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class SprintResponse(
    val id: String,
    val projectId: String,
    val name: String,
    val goal: String?,
    val status: String,
    val startDate: Instant,
    val endDate: Instant,
    val createdBy: String,
    val createdAt: Instant,
    val updatedAt: Instant
) {
    companion object {
        fun from(sprint: Sprint) = SprintResponse(
            id = sprint.id.toString(),
            projectId = sprint.projectId.toString(),
            name = sprint.name,
            goal = sprint.goal,
            status = sprint.status.name,
            startDate = sprint.startDate,
            endDate = sprint.endDate,
            createdBy = sprint.createdBy.toString(),
            createdAt = sprint.createdAt,
            updatedAt = sprint.updatedAt
        )
    }
}
