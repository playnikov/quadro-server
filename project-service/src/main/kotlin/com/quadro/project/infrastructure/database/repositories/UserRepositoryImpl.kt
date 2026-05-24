package com.quadro.project.infrastructure.database.repositories

import com.quadro.project.domain.models.User
import com.quadro.project.domain.repositories.UserRepository
import com.quadro.project.infrastructure.database.entities.UserEntity
import com.quadro.project.infrastructure.database.entities.UsersTable
import com.quadro.project.infrastructure.database.mappers.UserMapper
import com.quadro.shared.utils.toOffsetDateTime
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.time.Instant
import java.util.UUID
import kotlin.time.Clock

class UserRepositoryImpl : UserRepository {
    override suspend fun upsert(user: User): User = newSuspendedTransaction {
        val existing = UserEntity.findById(user.id)
        val entity = if (existing != null) {
            UserMapper.updateEntity(existing, user)
            existing
        } else {
            UserMapper.newEntity(user)
        }
        UserMapper.toDomain(entity)
    }

    override suspend fun findByIds(ids: Set<UUID>): List<User> = newSuspendedTransaction {
        UserEntity.find {
            UsersTable.id inList ids
        }.map { UserMapper.toDomain(it) }
    }

    override suspend fun findByEmail(email: String): User? = newSuspendedTransaction {
        UserEntity.find {
            UsersTable.email eq email
        }.firstOrNull()?.let(UserMapper::toDomain)
    }

    override suspend fun findById(id: UUID): User? = newSuspendedTransaction {
        UserEntity.findById(id)?.let(UserMapper::toDomain)
    }

    override suspend fun findByIds(ids: List<UUID>): List<User> = newSuspendedTransaction {
        UserEntity.find { UsersTable.id inList ids }
            .map { UserMapper.toDomain(it) }
    }

    override suspend fun delete(id: UUID): Boolean = newSuspendedTransaction {
        UserEntity.findById(id)?.delete() != null
    }
}