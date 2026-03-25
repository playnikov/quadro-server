package com.quadro.auth.presentation.models

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String,
    val lastName: String,
    val firstName: String,
    val middleName: String? = null
)
