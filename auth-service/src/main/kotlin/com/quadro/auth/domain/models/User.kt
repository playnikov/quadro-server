package com.quadro.auth.domain.models

import com.quadro.shared.dto.DomainException
import kotlinx.serialization.Serializable
import java.util.UUID
import kotlin.time.Instant

enum class UserRole {
    SUPER_ADMIN, ADMIN, USER;

    fun isAdmin() = this in listOf(SUPER_ADMIN, ADMIN)
    fun isSuperAdmin() = this == SUPER_ADMIN
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
    val isNeedChangePassword: Boolean = false,
    val createdAt: Instant,
    val updatedAt: Instant,
    val lastLoginAt: Instant? = null
)

data class UserCreate(
    val username: String,
    val email: String,
    val lastName: String,
    val firstName: String,
    val middleName: String?,
    val isNeedChangePassword: Boolean ? = false,
    val password: String
)

data class UserLogin(
    val name: String,
    val password: String,
)