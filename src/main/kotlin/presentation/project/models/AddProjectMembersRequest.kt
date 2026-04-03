package com.quadro.presentation.project.models

import kotlinx.serialization.Serializable

@Serializable
data class AddProjectMembersRequest(
    val userIds: List<String>,
    val role: String = "MEMBER"
)