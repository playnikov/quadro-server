package com.quadro.domain.models

import java.util.UUID

data class User(
    val id: UUID,
    val email: String,
    val username: String,
    val avatar: String?,
    val firstName: String?,
    val lastName: String?,
    val passwordHash: String,
    val role: DomainUserRole,
    val isActive: Boolean,
    val isEmailVerified: Boolean,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

enum class DomainUserRole {
    SUPER_ADMIN, ADMIN, PROJECT_MANAGER, TEAM_LEAD, USER, GUEST
}

data class UserCreate(
    val email: String,
    val username: String,
    val password: String,
    val firstName: String?,
    val lastName: String?,
    val role: DomainUserRole = DomainUserRole.USER,
)

data class UserLogin(
    val email: String? = null,
    val username: String? = null,
    val password: String,
)

data class UserResponse(
    val id: UUID,
    val email: String,
    val username: String,
    val firstName: String?,
    val lastName: String?,
    val role: DomainUserRole,
    val createdAt: Long = System.currentTimeMillis()
) {
    companion object {
        fun fromUser(user: User): UserResponse = UserResponse(
            id = user.id,
            email = user.email,
            username = user.username,
            firstName = user.firstName,
            lastName = user.lastName,
            role = user.role,
            createdAt = user.createdAt
        )
    }
}

data class AuthResponse(
    val token: String,
    val refreshToken: String,
    val tokenType: String = "Bearer"
)

data class RefreshTokenRequest(
    val refreshToken: String
)

data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String
)

data class ForgotPasswordRequest(
    val email: String
)

data class ResetPasswordRequest(
    val token: String,
    val newPassword: String
)

data class TokenValidationResult(
    val userId: UUID?,
    val isValid: Boolean,
    val isExpired: Boolean = false,
    val error: String? = null
)