package com.quadro.domain.models.project

import java.util.UUID

data class AssignTeam(
    val teamId: UUID,
    val role: ProjectRole = ProjectRole.MEMBER,
    val isLeadTeam: Boolean = false
)

data class UpdateTeamRole(
    val role: ProjectRole,
    val isLeadTeam: Boolean? = null
)