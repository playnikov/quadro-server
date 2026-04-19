package com.quadro.company.domain.repositories

import com.quadro.company.domain.models.User
import java.util.UUID

interface UserRepository {
    suspend fun upsert(user: User): User
    suspend fun findByIds(ids: Set<UUID>): List<User>
    suspend fun findById(id: UUID): User?
    suspend fun delete(id: UUID): Boolean
}