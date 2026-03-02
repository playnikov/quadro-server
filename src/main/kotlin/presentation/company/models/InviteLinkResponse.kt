package com.quadro.presentation.company.models

import kotlinx.serialization.Serializable

@Serializable
data class InviteLinkResponse(
    val link: String,
    val expiresAt: String
)
