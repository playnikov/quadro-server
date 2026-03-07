package com.quadro.domain.models.project

import java.util.UUID

data class ProjectTeamAssignment(
    val teamId: UUID,
    val role: ProjectRole = ProjectRole.MEMBER,
    val isLeadTeam: Boolean = false
)