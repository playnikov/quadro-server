package com.quadro.domain.models.project

import java.util.UUID

data class Project(
    val id: UUID,
    val companyId: UUID,
    val type: ProjectType,
    val name: String,
    val key: String,
    val description: String?,
    val status: ProjectStatus,
    val priority: ProjectPriority,
    val visibility: ProjectVisibility,
    val leadId: UUID,
    val ownerId: UUID,
    val settings: ProjectSettings,
    val startDate: Long?,
    val endDate: Long?,
    val completedAt: Long?,
    val createdAt: Long,
    val updatedAt: Long,
    val archivedAt: Long?,
)