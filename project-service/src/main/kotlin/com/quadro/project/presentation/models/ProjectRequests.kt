package com.quadro.project.presentation.models

import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class ProjectCreateRequest(
    val type: String,
    val name: String,
    val key: String,
    val description: String? = null,
    val priority: String,
    val visibility: String,
    val startDate: Instant? = null,
    val endDate: Instant? = null,
    val leadId: String
)

@Serializable
data class ProjectUpdateRequest(
    val name: String?,
    val description: String?,
    val status: String?,
    val priority: String?,
    val visibility: String?,
    val startDate: Instant?,
    val endDate: Instant?,
    val leadId: String?
)