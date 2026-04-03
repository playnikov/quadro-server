package com.quadro.presentation.project.models

import kotlinx.serialization.Serializable

@Serializable
data class UpdateProjectRequest(
    val name: String? = null,
    val description: String? = null,
    val status: String? = null,
    val priority: String? = null,
    val visibility: String? = null,
    val leadId: String? = null,
    val startDate: Long? = null,
    val endDate: Long? = null
)
