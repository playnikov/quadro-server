package com.quadro.presentation.auth.models

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val email: String,
    val username: String,
    val password: String,
    val lastName: String,
    val firstName: String
)
