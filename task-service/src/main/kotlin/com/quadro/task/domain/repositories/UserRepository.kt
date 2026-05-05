package com.quadro.task.domain.repositories

import com.quadro.task.domain.models.User
import java.util.UUID

interface UserRepository {
    suspend fun upsert(user: User): User
    suspend fun findById(id: UUID): User?
    suspend fun delete(id: UUID): Boolean
}