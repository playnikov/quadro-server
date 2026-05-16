package com.quadro.project.presentation.models

import com.quadro.project.domain.models.ProjectRole
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class ProjectCreateRequest(
    val type: String,
    val name: String,
    val key: String,
    val description: String? = null,
    val priority: String,
    val visibility: String
)

@Serializable
data class ProjectUpdateRequest(
    val name: String? = null,
    val description: String? = null,
    val status: String? = null,
    val priority: String? = null,
    val visibility: String? = null
)

@Serializable
data class UpdateMemberRole(
    val role: ProjectRole
)