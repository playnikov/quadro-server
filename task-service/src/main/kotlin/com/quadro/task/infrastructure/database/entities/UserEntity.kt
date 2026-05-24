package com.quadro.task.infrastructure.database.entities

import com.quadro.task.domain.models.UserRole
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.javatime.timestampWithTimeZone
import org.postgresql.util.PGobject
import java.util.UUID

object UsersTable : UUIDTable("users_copy") {
    val email = varchar("email", 255)
    val firstName = varchar("first_name", 100)
    val lastName = varchar("last_name", 100)
    val middleName = varchar("middle_name", 100).nullable()
    val role = customEnumeration(
        name = "role",
        sql = "user_roles",
        fromDb = { value -> UserRole.valueOf(value as String) },
        toDb = { userRole ->
            PGobject().apply {
                type = "user_roles"
                value = userRole.name
            }
        }
    )
    val isActive = bool("is_active").default(true)
}

class UserEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<UserEntity>(UsersTable)

    var email by UsersTable.email
    var firstName by UsersTable.firstName
    var lastName by UsersTable.lastName
    var middleName by UsersTable.middleName
    var role by UsersTable.role
    var isActive by UsersTable.isActive
}