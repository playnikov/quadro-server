package com.quadro.task.domain.models

import java.util.UUID
import kotlin.time.Instant

enum class UserRole {
    SUPER_ADMIN, ADMIN, PROJECT_MANAGER, USER
}

data class User(
    val id: UUID,
    val email: String,
    val lastName: String,
    val firstName: String,
    val middleName: String? = null,
    val role: UserRole,
    val isActive: Boolean
)
