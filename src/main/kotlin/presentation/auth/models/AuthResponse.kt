package com.quadro.presentation.auth.models

import kotlinx.serialization.Serializable

@Serializable
data class AuthResponse(
    val token: String,
    val refreshToken: String,
    val type: String = "Bearer"
)
