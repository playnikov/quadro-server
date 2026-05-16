package com.quadro.auth.presentation.models

import kotlinx.serialization.Serializable

@Serializable
data class ChangePasswordRequest(
    val currentPassword: String? = null,
    val newPassword: String
)
