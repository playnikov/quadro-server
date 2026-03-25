package com.quadro.auth.presentation.models

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val username: String? = null,
    val email: String? = null,
    val password: String
)