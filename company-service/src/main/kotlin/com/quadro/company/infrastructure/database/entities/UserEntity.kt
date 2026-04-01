package com.quadro.company.infrastructure.database.entities

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.javatime.timestampWithTimeZone
import java.util.UUID

object UsersTable : UUIDTable("users_copy") {
    val email = varchar("email", 255).uniqueIndex()
    val firstName = varchar("first_name", 100)
    val lastName = varchar("last_name", 100)
    val middleName = varchar("middle_name", 100).nullable()
    val avatarUrl = varchar("avatar", 500).nullable()
    val role = varchar("role", 50)
    val isActive = bool("is_active").default(true)
    val updatedAt = timestampWithTimeZone("updated_at")
}

class UserEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<UserEntity>(UsersTable)

    var email by UsersTable.email
    var firstName by UsersTable.firstName
    var lastName by UsersTable.lastName
    var middleName by UsersTable.middleName
    var avatar by UsersTable.avatarUrl
    var role by UsersTable.role
    var isActive by UsersTable.isActive
    var updatedAt by UsersTable.updatedAt
}