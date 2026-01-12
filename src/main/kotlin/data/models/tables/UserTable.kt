package com.quadro.data.models.tables

import com.quadro.data.models.UserRole
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime

object UserTable : Table("users") {
    val id = long("id").autoIncrement()
    val email = varchar("email", 255).uniqueIndex()
    val passwordHash = varchar("password_hash", 255)
    val lastName = varchar("last_name", 100)
    val firstName = varchar("first_name", 100)
    val middleName = varchar("middle_name", 100).nullable()
    val role = customEnumeration(
        "role",
        "user_role",
        { value -> UserRole.valueOf(value as String) },
        { it -> it.name }
    )
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")

    override val primaryKey = PrimaryKey(id)
}