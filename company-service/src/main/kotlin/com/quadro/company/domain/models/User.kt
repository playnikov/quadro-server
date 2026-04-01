package com.quadro.company.domain.models

import kotlinx.serialization.Serializable
import java.util.UUID
import kotlin.time.Instant

data class User(
    val id: UUID,
    val email: String,
    val firstName: String,
    val lastName: String,
    val middleName: String?,
    val avatar: String?,
    val role: String,
    val isActive: Boolean,
    val updatedAt: Instant
)

@Serializable
data class UserResponse(
    val id: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val middleName: String?,
    val avatar: String?,
    val role: String,
    val isActive: Boolean,
    val updatedAt: Instant
) {
    companion object {
        fun fromUser(user: User): UserResponse = UserResponse(
            id = user.id.toString(),
            email = user.email,
            firstName = user.firstName,
            lastName = user.lastName,
            middleName = user.middleName,
            avatar = user.avatar,
            role = user.role,
            isActive = user.isActive,
            updatedAt = user.updatedAt
        )
    }
}
