package com.quadro.domain.models.project

import java.util.UUID

data class ProjectTeam(
    val id: UUID,
    val projectId: UUID,
    val teamId: UUID,
    val role: ProjectRole,
    val isLeadTeam: Boolean,
    val assignedAt: Long,
    val assignedBy: UUID
)