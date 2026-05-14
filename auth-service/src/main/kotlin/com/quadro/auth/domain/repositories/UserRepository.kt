package com.quadro.auth.domain.repositories

import com.quadro.auth.domain.models.User
import java.util.UUID

interface UserRepository {
    suspend fun upsert(user: User): User
    suspend fun getAll(): List<User>
    suspend fun getByIds(ids: List<UUID>): List<User>
    suspend fun findByEmail(email: String): User?
    suspend fun findByUsername(username: String): User?
    suspend fun findById(id: UUID): User?
    suspend fun delete(id: UUID): Boolean
    suspend fun existsByEmail(email: String): Boolean
    suspend fun existsByUsername(username: String): Boolean
    suspend fun updateLastLogin(id: UUID, ip: String?): Boolean
}