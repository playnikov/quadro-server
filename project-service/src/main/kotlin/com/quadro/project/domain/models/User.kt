package com.quadro.project.domain.models

import java.util.UUID

enum class UserRole {
    SUPER_ADMIN, ADMIN, USER
}

data class User(
    val id: UUID,
    val role: UserRole,
    val isActive: Boolean,
)
