package com.quadro.task.infrastructure.database.repositories

import com.quadro.shared.utils.toOffsetDateTime
import com.quadro.task.domain.models.User
import com.quadro.task.domain.repositories.UserRepository
import com.quadro.task.infrastructure.database.entities.UserEntity
import com.quadro.task.infrastructure.database.mappers.UserMapper
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.time.Instant
import java.util.UUID
import kotlin.time.Clock

class UserRepositoryImpl : UserRepository {
    override suspend fun upsert(user: User) = newSuspendedTransaction {
        val existing = UserEntity.findById(user.id)
        val entity = if (existing != null) {
            UserMapper.updateEntity(existing, user)
            existing
        } else {
            UserMapper.newEntity(user)
        }
        UserMapper.toDomain(entity)
    }

    override suspend fun findById(id: UUID): User? = newSuspendedTransaction {
        UserEntity.findById(id)?.let(UserMapper::toDomain)
    }

    override suspend fun delete(id: UUID): Boolean = newSuspendedTransaction {
        UserEntity.findById(id)?.delete() != null
    }
}