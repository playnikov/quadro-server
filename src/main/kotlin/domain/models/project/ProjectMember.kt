package com.quadro.domain.models.project

import java.util.UUID

data class ProjectMember(
    val id: UUID,
    val projectId: UUID,
    val userId: UUID,
    val role: ProjectRole,
    val joinedAt: Long,
    val invitedBy: UUID,
    val invitedAt: Long,
    val sourceTeamId: UUID?
)