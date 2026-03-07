package com.quadro.presentation.project.models

import kotlinx.serialization.Serializable

@Serializable
data class CreateProjectRequest(
    val companyId: String
)
