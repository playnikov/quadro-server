package com.quadro.datasource.repositories.users

import com.quadro.domain.models.user.User
import java.util.UUID

interface UserRepository {
    suspend fun create(user: User): User
    suspend fun findByEmail(email: String): User?
    suspend fun findByUsername(username: String): User?
    suspend fun findById(id: UUID): User?
    suspend fun update(user: User): User
    suspend fun delete(id: UUID): Boolean
    suspend fun existsByEmail(email: String): Boolean
    suspend fun existsByUsername(username: String): Boolean
    suspend fun findAll(limit: Int, offset: Int): List<User>
    suspend fun count(): Long
}