package com.quadro.data.repositories

import com.quadro.config.DatabaseFactory.dbQuery
import com.quadro.data.models.UserModel
import com.quadro.data.models.UserRole
import com.quadro.data.models.extensions.toUser
import com.quadro.data.models.tables.UserTable
import com.quadro.domain.repositories.UserRepository
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import kotlin.math.sin

class UserRepositoryImpl : UserRepository {
    override suspend fun createUser(user: UserModel): UserModel = dbQuery {
        UserTable.insert { table ->
                table[email] = user.email
                table[passwordHash] = user.passwordHash
                table[lastName] = user.lastName
                table[firstName] = user.firstName
                table[middleName] = user.middleName
                table[role] = user.role
                table[createdAt] = user.createdAt
                table[updatedAt] = user.updatedAt
            }.resultedValues?.singleOrNull()?.toUser() ?: user
    }

    override suspend fun updateUser(user: UserModel): UserModel = dbQuery {
        UserTable.update({ UserTable.id.eq(user.id) }) { table ->
            table[email] = user.email
            table[passwordHash] = user.passwordHash
            table[lastName] = user.lastName
            table[firstName] = user.firstName
            table[middleName] = user.middleName
            table[role] = user.role
            table[updatedAt] = user.updatedAt
        }
        user
    }

    override suspend fun deleteUser(id: Long): Boolean = dbQuery {
        UserTable.deleteWhere { UserTable.id eq id } > 0
    }

    override suspend fun findUserById(id: Long): UserModel? = dbQuery {
        UserTable.selectAll()
            .where { UserTable.id eq id }
            .map { it.toUser() }
            .singleOrNull()
    }

    override suspend fun findUserByEmail(email: String): UserModel? = dbQuery {
        UserTable.selectAll()
            .where { UserTable.email eq email }
            .map { it.toUser() }
            .singleOrNull()
    }

    override suspend fun findAll(page: Int, pageSize: Int): List<UserModel> = dbQuery {
        UserTable.selectAll()
            .limit(pageSize).offset(start = (page * pageSize).toLong())
            .mapNotNull { it.toUser() }
    }

    override suspend fun existsByEmail(email: String): Boolean = dbQuery {
        UserTable.selectAll()
            .where { UserTable.email eq email }
            .count() > 0
    }
}