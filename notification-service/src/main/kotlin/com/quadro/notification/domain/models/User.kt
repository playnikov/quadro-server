package com.quadro.notification.domain.models

import java.util.UUID

data class User(
    val id: UUID,
    val role: UserRole,
    val isActive: Boolean
)

enum class UserRole {
    ADMIN, MANAGER, MEMBER
}