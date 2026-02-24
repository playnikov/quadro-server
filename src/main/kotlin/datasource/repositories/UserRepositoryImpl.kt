package com.quadro.datasource.repositories

import com.quadro.datasource.entities.UserEntity
import com.quadro.datasource.entities.UsersTable
import com.quadro.datasource.mappers.UserMapper
import com.quadro.domain.models.User
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.UUID

class UserRepositoryImpl : UserRepository {
    override suspend fun create(user: User): User = newSuspendedTransaction {
        val entity = UserMapper.toEntity(user)
        UserMapper.toDomain(entity)
    }

    override suspend fun update(user: User): User = newSuspendedTransaction {
        val entity = UserEntity.findById(user.id) ?: throw IllegalArgumentException("User not found")
        UserMapper.updateEntity(entity, user)
        UserMapper.toDomain(entity)
    }

    override suspend fun delete(id: UUID): Boolean = newSuspendedTransaction {
        UserEntity.findById(id)?.delete() != null
    }

    override suspend fun findById(id: UUID): User? = newSuspendedTransaction {
        UserEntity.findById(id)?.let { UserMapper.toDomain(it) }
    }

    override suspend fun findByEmail(email: String): User? = newSuspendedTransaction {
        UserEntity.find { UsersTable.email eq email }.firstOrNull()?.let { UserMapper.toDomain(it) }
    }

    override suspend fun findByUsername(username: String): User? = newSuspendedTransaction {
        UserEntity.find { UsersTable.username eq username }.firstOrNull()?.let { UserMapper.toDomain(it) }
    }

    override suspend fun findAll(limit: Int, offset: Int): List<User> = newSuspendedTransaction {
        UserEntity.all().limit(limit).offset(offset.toLong()).map { UserMapper.toDomain(it) }
    }

    override suspend fun existsByEmail(email: String): Boolean = newSuspendedTransaction {
        !UserEntity.find { UsersTable.email eq email }.empty()
    }

    override suspend fun existsByUsername(username: String): Boolean = newSuspendedTransaction {
        !UserEntity.find { UsersTable.username eq username }.empty()
    }

    override suspend fun count(): Long = newSuspendedTransaction {
        UserEntity.all().count()
    }
}