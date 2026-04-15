package com.quadro.team.presentation.models

import kotlinx.serialization.Serializable

@Serializable
data class BindingRequest(
    val projectId: String,
    val role: String
)

@Serializable
data class UnbindRequest(
    val projectId: String
)
