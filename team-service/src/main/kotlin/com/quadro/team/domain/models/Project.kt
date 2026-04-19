package com.quadro.team.domain.models

import kotlinx.serialization.Serializable
import java.util.UUID
import kotlin.time.Instant

@Serializable
enum class ProjectStatus {
    ACTIVE, ON_HOLD, COMPLETED, ARCHIVED, CANCELLED
}

data class Project(
    val id: UUID,
    val companyId: UUID,
    val status: ProjectStatus,
    val updatedAt: Instant,
)