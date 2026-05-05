package com.quadro.task.domain.models.project

import kotlinx.serialization.Serializable
import java.util.UUID
import kotlin.time.Instant

@Serializable
enum class ProjectStatus {
    ACTIVE, ON_HOLD, COMPLETED, ARCHIVED, CANCELLED
}

data class Project(
    val id: UUID,
    val key: String,
    val status: ProjectStatus
)