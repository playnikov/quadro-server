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
    val endDate: Instant? = null
)

@Serializable
data class ProjectUpdateRequest(
    val name: String? = null,
    val description: String? = null,
    val status: String? = null,
    val priority: String? = null,
    val visibility: String? = null,
    val startDate: Instant? = null,
    val endDate: Instant? = null
)