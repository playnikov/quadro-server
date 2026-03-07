package com.quadro.domain.models.project

import java.util.UUID

data class ProjectCreate(
    val companyId: UUID,
    val type: ProjectType = ProjectType.TEAM_MANAGED,
    val name: String,
    val key: String,
    val description: String? = null,
    val priority: ProjectPriority = ProjectPriority.MEDIUM,
    val visibility: ProjectVisibility = ProjectVisibility.RESTRICTED,
    val settings: ProjectSettings? = null,
    val startDate: Long? = null,
    val endDate: Long? = null,
    val leadId: UUID,
    val initialTeams: List<ProjectTeamAssignment>? = null
)