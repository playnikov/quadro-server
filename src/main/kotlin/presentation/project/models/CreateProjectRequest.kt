package com.quadro.presentation.project.models

import kotlinx.serialization.Serializable

@Serializable
data class CreateProjectRequest(
    val companyId: String,
    val type: String = "TEAM_MANAGED",
    val name: String,
    val key: String,
    val description: String? = null,
    val priority: String = "MEDIUM",
    val visibility: String = "RESTRICTED",
    val leadId: String,
    val startDate: Long? = null,
    val endDate: Long? = null,
    val initialTeams: List<ProjectTeamAssignmentRequest>? = null
)
