package com.quadro.presentation.project.models

import kotlinx.serialization.Serializable

@Serializable
data class ProjectTeamAssignmentRequest(
    val teamId: String,
    val role: String = "MEMBER",
    val isLeadTeam: Boolean = false
)