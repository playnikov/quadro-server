package com.quadro.project.presentation.models

import com.quadro.project.domain.models.MemberRole
import kotlinx.serialization.Serializable

@Serializable
data class ProjectCreateRequest(
    val name: String,
    val key: String,
    val description: String? = null,
)

@Serializable
data class ProjectUpdateRequest(
    val name: String? = null,
    val description: String? = null,
    val status: String? = null,
)

@Serializable
data class UpdateMemberRole(
    val role: MemberRole
)