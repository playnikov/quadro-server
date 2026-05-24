package com.quadro.auth.infrastructure.database.repositories

import com.quadro.auth.domain.models.User
import com.quadro.auth.domain.repositories.UserRepository
import com.quadro.auth.infrastructure.database.entities.UserEntity
import com.quadro.auth.infrastructure.database.entities.UsersTable
import com.quadro.auth.infrastructure.database.mappers.UserMapper
import com.quadro.shared.utils.toOffsetDateTime
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.time.Instant
import java.util.UUID
import kotlin.time.Clock

class UserRepositoryImpl : UserRepository {
    override suspend fun upsert(user: User): User  = newSuspendedTransaction {
        val existing = UserEntity.findById(user.id)
        val entity = if (existing != null) {
            UserMapper.updateEntity(existing, user)
            existing
        } else {
            UserMapper.toEntity(user)
        }
        UserMapper.toDomain(entity)
    }

    override suspend fun getAll(): List<User> = newSuspendedTransaction {
        UserEntity.all()
            .map { UserMapper.toDomain(it) }
    }

    override suspend fun getByIds(ids: List<UUID>): List<User> = newSuspendedTransaction {
        UserEntity.find { UsersTable.id inList ids }
            .map { UserMapper.toDomain(it) }
    }

    override suspend fun findByEmail(email: String): User? = newSuspendedTransaction {
        UserEntity.find { UsersTable.email eq email }.firstOrNull()?.let { UserMapper.toDomain(it) }
    }

    override suspend fun findByUsername(username: String): User? = newSuspendedTransaction {
        UserEntity.find { UsersTable.username eq username }.firstOrNull()?.let { UserMapper.toDomain(it) }
    }

    override suspend fun findById(id: UUID): User? = newSuspendedTransaction {
        UserEntity.findById(id)?.let { UserMapper.toDomain(it) }
    }

    override suspend fun delete(id: UUID): Boolean = newSuspendedTransaction {
        UserEntity.findById(id)?.delete() != null
    }

    override suspend fun existsByEmail(email: String): Boolean = newSuspendedTransaction {
        !UserEntity.find { UsersTable.email eq email }.empty()
    }

    override suspend fun existsByUsername(username: String): Boolean = newSuspendedTransaction {
        !UserEntity.find { UsersTable.username eq username }.empty()
    }

    override suspend fun updateLastLogin(id: UUID, ip: String?): Boolean = newSuspendedTransaction {
        UserEntity.findById(id)?.apply {
            lastLoginAt = Clock.System.now().toOffsetDateTime()
        } != null
    }
}