package com.quadro.task.domain.models

import java.util.UUID

enum class UserRole {
    SUPER_ADMIN, ADMIN, PROJECT_MANAGER, USER
}

data class User(
    val id: UUID,
    val role: UserRole,
    val isActive: Boolean,
)
