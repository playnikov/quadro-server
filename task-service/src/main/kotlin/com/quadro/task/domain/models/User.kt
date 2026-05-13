package com.quadro.task.domain.models

import java.util.UUID

enum class UserRole {
    SUPER_ADMIN, ADMIN, PROJECT_MANAGER, USER
}

data class User(
    val id: UUID,
    val lastName: String,
    val firstName: String,
    val role: UserRole,
    val isActive: Boolean,
)
