package com.quadro.auth.domain.models

import kotlinx.serialization.Serializable
import java.util.UUID
import kotlin.time.Instant

enum class UserRole {
    SUPER_ADMIN, ADMIN, PROJECT_MANAGER, USER
}

data class User(
    val id: UUID,
    val username: String,
    val email: String,
    val passwordHash: String,
    val firstName: String,
    val lastName: String,
    val middleName: String?,
    val avatarUrl: String? = null,
    val role: UserRole,
    val isActive: Boolean = true,
    val isEmailVerified: Boolean = false,
    val createdAt: Instant,
    val updatedAt: Instant,
    val lastLoginAt: Instant? = null,
    val lastLoginIp: String? = null
)

data class UserCreate(
    val username: String,
    val email: String,
    val lastName: String,
    val firstName: String,
    val middleName: String?,
    val password: String
)

data class UserLogin(
    val email: String? = null,
    val username: String? = null,
    val password: String,
)

@Serializable
data class UserResult(
    val id: String,
    val username: String,
    val email: String,
    val lastName: String,
    val firstName: String,
    val middleName: String?,
    val avatarUrl: String?,
    val role: String,
    val isActive: Boolean,
    val isEmailVerified: Boolean,
    val createdAt: Instant,
    val lastLoginAt: Instant?
) {
    companion object {
        fun fromUser(user: User): UserResult = UserResult(
            id = user.id.toString(),
            username = user.username,
            email = user.email,
            lastName = user.lastName,
            firstName = user.firstName,
            middleName = user.middleName,
            avatarUrl = user.avatarUrl,
            role = user.role.name,
            isActive = user.isActive,
            isEmailVerified = user.isEmailVerified,
            createdAt = user.createdAt,
            lastLoginAt = user.lastLoginAt
        )
    }
}

@Serializable
data class AuthResult(
    val token: String,
    val refreshToken: String,
    val tokenType: String = "Bearer",
    val userInfo: UserResult
)