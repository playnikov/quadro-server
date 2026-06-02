package com.quadro.auth.presentation.models

import com.quadro.auth.domain.models.User
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class UserResponse(
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
    val isNeedChangePassword: Boolean,
    val createdAt: Instant,
    val lastLoginAt: Instant?
) {
    companion object {
        fun from(user: User): UserResponse = UserResponse(
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
            isNeedChangePassword = user.isNeedChangePassword,
            createdAt = user.createdAt,
            lastLoginAt = user.lastLoginAt
        )
    }
}