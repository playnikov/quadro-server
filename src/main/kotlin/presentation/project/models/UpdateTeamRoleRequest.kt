package com.quadro.presentation.project.models

import kotlinx.serialization.Serializable

@Serializable
data class UpdateTeamRoleRequest(
    val role: String,
    val isLeadTeam: Boolean? = null
)