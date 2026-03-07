package com.quadro.domain.models.project

import java.util.UUID

data class ProjectUpdate(
    val name: String? = null,
    val description: String? = null,
    val status: ProjectStatus? = null,
    val priority: ProjectPriority? = null,
    val visibility: ProjectVisibility? = null,
    val settings: ProjectSettings? = null,
    val startDate: Long? = null,
    val endDate: Long? = null,
    val leadId: UUID? = null
)