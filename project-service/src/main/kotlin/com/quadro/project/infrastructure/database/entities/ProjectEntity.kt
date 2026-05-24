package com.quadro.project.infrastructure.database.entities

import com.quadro.project.domain.models.ProjectStatus
import com.quadro.project.domain.models.UserRole
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.javatime.timestampWithTimeZone
import org.postgresql.util.PGobject
import java.time.Instant
import java.util.UUID

object ProjectsTable : UUIDTable("projects") {
    val name = varchar("name", 50)
    val key = varchar("key", 20)
    val description = text("description").nullable()
    val status = customEnumeration(
        name = "status",
        sql = "project_status",
        fromDb = { value -> ProjectStatus.valueOf(value as String) },
        toDb = { status ->
            PGobject().apply {
                type = "project_status"
                value = status.name
            }
        }
    )
    val createdAt = timestampWithTimeZone("created_at")
    val updatedAt = timestampWithTimeZone("updated_at")
}

class ProjectEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<ProjectEntity>(ProjectsTable)

    var name by ProjectsTable.name
    var key by ProjectsTable.key
    var description by ProjectsTable.description
    var status by ProjectsTable.status
    var createdAt by ProjectsTable.createdAt
    var updatedAt by ProjectsTable.updatedAt
}