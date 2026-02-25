package com.quadro.datasource.entities

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import java.time.Instant
import org.jetbrains.exposed.sql.javatime.timestamp
import java.util.UUID

object UsersTable : UUIDTable("users") {
    val email = varchar("email", 255).uniqueIndex()
    val username = varchar("username", 100).uniqueIndex()
    val avatar = varchar("avatar", 500).nullable()
    val passwordHash = varchar("password_hash", 255)
    val firstName = varchar("first_name", 100).nullable()
    val lastName = varchar("last_name", 100).nullable()
    val role = enumerationByName("role", 50, DbUserRole::class)
    val isActive = bool("is_active").default(true)
    val isEmailVerified = bool("is_email_verified").default(false)
    val createdAt = timestamp("created_at").default(Instant.now())
    val updatedAt = timestamp("updated_at").default(Instant.now())
}

enum class DbUserRole {
    SUPER_ADMIN, ADMIN, PROJECT_MANAGER, TEAM_LEAD, USER, GUEST
}

class UserEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<UserEntity>(UsersTable)

    var email by UsersTable.email
    var username by UsersTable.username
    var avatar by UsersTable.avatar
    var passwordHash by UsersTable.passwordHash
    var firstName by UsersTable.firstName
    var lastName by UsersTable.lastName
    var role by UsersTable.role
    var isActive by UsersTable.isActive
    var isEmailVerified by UsersTable.isEmailVerified
    var createdAt by UsersTable.createdAt
    var updatedAt by UsersTable.updatedAt
}