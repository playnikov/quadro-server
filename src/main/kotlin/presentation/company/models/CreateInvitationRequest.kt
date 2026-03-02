package com.quadro.presentation.company.models

import kotlinx.serialization.Serializable

@Serializable
data class CreateInvitationRequest(
    val teamId: String? = null,
    val role: String = "MEMBER",
    val message: String? = null,
    val expiresInDays: Int? = null
)
