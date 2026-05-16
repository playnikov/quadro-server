package com.quadro.auth.presentation.models

import com.quadro.auth.domain.models.UserRole
import kotlinx.serialization.Serializable

@Serializable
data class UpdateUserRequest(
    val email: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val middleName: String? = null
)

@Serializable
data class UpdateAdminUserRequest(
    val username: String? = null,
    val email: String? = null,
    val password: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val middleName: String? = null,
    val role: UserRole? = null,
    val isActive: Boolean? = null,
)
