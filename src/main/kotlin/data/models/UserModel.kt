package com.quadro.data.models

import java.time.LocalDateTime

data class UserModel(
    val id: Long,
    val email: String,
    val passwordHash: String,
    val lastName: String,
    val firstName: String,
    val middleName: String? = null,
    val role: UserRole,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

enum class UserRole {
    ADMIN, MANAGER, EXECUTOR
}