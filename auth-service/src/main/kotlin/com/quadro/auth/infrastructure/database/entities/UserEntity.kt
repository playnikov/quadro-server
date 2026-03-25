package com.quadro.auth.infrastructure.database.entities

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.javatime.timestampWithTimeZone
import java.util.UUID

object UsersTable : UUIDTable("users") {
    val username = varchar("username", 100).uniqueIndex()
    val email = varchar("email", 255).uniqueIndex()
    val passwordHash = varchar("password_hash", 255)
    val firstName = varchar("first_name", 100)
    val lastName = varchar("last_name", 100)
    val middleName = varchar("middle_name", 100).nullable()
    val avatarUrl = varchar("avatar", 500).nullable()
    val role = varchar("role", 50)
    val isActive = bool("is_active").default(true)
    val isEmailVerified = bool("is_email_verified").default(false)
    val createdAt = timestampWithTimeZone("created_at")
    val updatedAt = timestampWithTimeZone("updated_at")
    val lastLoginAt = timestampWithTimeZone("last_login_at").nullable()
    val lastLoginIp = varchar("last_login_ip", 45).nullable()
}

class UserEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<UserEntity>(UsersTable)

    var email by UsersTable.email
    var username by UsersTable.username
    var passwordHash by UsersTable.passwordHash
    var firstName by UsersTable.firstName
    var lastName by UsersTable.lastName
    var middleName by UsersTable.middleName
    var avatar by UsersTable.avatarUrl
    var role by UsersTable.role
    var isActive by UsersTable.isActive
    var isEmailVerified by UsersTable.isEmailVerified
    var createdAt by UsersTable.createdAt
    var updatedAt by UsersTable.updatedAt
    var lastLoginAt by UsersTable.lastLoginAt
    var lastLoginIp by UsersTable.lastLoginIp
}