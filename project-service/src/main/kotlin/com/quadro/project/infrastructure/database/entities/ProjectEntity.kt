package com.quadro.project.infrastructure.database.entities

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.javatime.timestampWithTimeZone
import java.time.Instant
import java.util.UUID

object ProjectsTable : UUIDTable("projects") {
    val type = varchar("type", 50)
    val name = varchar("name", 50)
    val key = varchar("key", 20)
    val description = text("description").nullable()
    val status = varchar("status", 50)
    val priority = varchar("priority", 50)
    val visibility = varchar("visibility", 50)
    val createdAt = timestampWithTimeZone("created_at")
    val updatedAt = timestampWithTimeZone("updated_at")
}

class ProjectEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<ProjectEntity>(ProjectsTable)

    var type by ProjectsTable.type
    var name by ProjectsTable.name
    var key by ProjectsTable.key
    var description by ProjectsTable.description
    var status by ProjectsTable.status
    var priority by ProjectsTable.priority
    var visibility by ProjectsTable.visibility
    var createdAt by ProjectsTable.createdAt
    var updatedAt by ProjectsTable.updatedAt
}