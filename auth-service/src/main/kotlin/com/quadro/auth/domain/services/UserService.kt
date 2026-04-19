package com.quadro.auth.domain.services

import com.quadro.auth.domain.models.User
import java.util.UUID

interface UserService {
    suspend fun getUserById(id: UUID): User
    suspend fun getAllUsers(): List<User>
    suspend fun getUserByUsername(username: String): User
    suspend fun getUserByEmail(email: String): User
    suspend fun getUsersByIds(userIds: List<UUID>): List<User>
}