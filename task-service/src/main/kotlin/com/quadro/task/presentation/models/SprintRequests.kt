package com.quadro.task.presentation.models

import com.quadro.task.domain.models.task.SprintStatus
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class SprintCreateRequest(
    val projectId: String,
    val name: String,
    val goal: String?,
    val status: SprintStatus,
    val startDate: Instant,
    val endDate: Instant,
    val createdBy: String
)

@Serializable
data class SprintUpdateRequest(
    val name: String? = null,
    val goal: String? = null,
    val status: SprintStatus? = null,
    val startDate: Instant? = null,
    val endDate: Instant? = null,
)