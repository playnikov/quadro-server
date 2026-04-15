package com.quadro.team.domain.models

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
    val isActive: Boolean,
)

@Serializable
data class UserResponse(
    val id: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val middleName: String?,
    val avatar: String?,
    val isActive: Boolean,
) {
    companion object {
        fun from(user: User): UserResponse = UserResponse(
            id = user.id.toString(),
            email = user.email,
            firstName = user.firstName,
            lastName = user.lastName,
            middleName = user.middleName,
            avatar = user.avatar,
            isActive = user.isActive,
        )
    }
}
