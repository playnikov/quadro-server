package com.quadro.project.domain.repositories

import com.quadro.project.domain.models.User
import java.util.UUID

interface UserRepository {
    suspend fun upsert(user: User): User
    suspend fun findByIds(ids: Set<UUID>): List<User>
    suspend fun findByEmail(email: String): User?
    suspend fun findById(id: UUID): User?
    suspend fun findByIds(ids: List<UUID>): List<User>
    suspend fun delete(id: UUID): Boolean
}