package com.quadro.project.domain.models

import kotlinx.serialization.Serializable
import java.util.UUID
import kotlin.time.Instant

@Serializable
enum class ProjectType {
    TEAM_MANAGED, COMPANY_MANAGED
}

@Serializable
enum class ProjectStatus {
    ACTIVE, ON_HOLD, COMPLETED, ARCHIVED, CANCELLED
}

@Serializable
enum class ProjectPriority {
    HIGHEST, HIGH, MEDIUM, LOW, LOWEST
}

@Serializable
enum class ProjectVisibility {
    PUBLIC, RESTRICTED, PRIVATE
}

data class Project(
    val id: UUID,
    val type: ProjectType,
    val name: String,
    val key: String,
    val description: String?,
    val status: ProjectStatus,
    val priority: ProjectPriority,
    val visibility: ProjectVisibility,
    val startDate: Instant?,
    val endDate: Instant?,
    val completedAt: Instant?,
    val createdAt: Instant,
    val updatedAt: Instant,
)

data class ProjectCreate(
    val type: ProjectType = ProjectType.TEAM_MANAGED,
    val name: String,
    val key: String,
    val description: String? = null,
    val priority: ProjectPriority = ProjectPriority.MEDIUM,
    val visibility: ProjectVisibility = ProjectVisibility.RESTRICTED,
    val startDate: Instant? = null,
    val endDate: Instant? = null
) {
    fun validate() {
        require(name.isNotBlank()) { "Name must not be blank" }
        require(name.length > 4) { "Name must not exceed 4 characters" }
    }
}

data class ProjectUpdate(
    val name: String? = null,
    val description: String? = null,
    val status: ProjectStatus? = null,
    val priority: ProjectPriority? = null,
    val visibility: ProjectVisibility? = null,
    val startDate: Instant? = null,
    val endDate: Instant? = null,
)