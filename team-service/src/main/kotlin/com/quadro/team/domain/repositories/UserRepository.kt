package com.quadro.team.domain.repositories

import com.quadro.team.domain.models.User
import java.util.UUID

interface UserRepository {
    suspend fun upsert(user: User): User
    suspend fun findById(id: UUID): User?
    suspend fun delete(id: UUID): Boolean
}