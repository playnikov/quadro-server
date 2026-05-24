package com.quadro.task.infrastructure.database.entities.project

import com.quadro.task.domain.models.project.ProjectStatus
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.javatime.timestampWithTimeZone
import org.postgresql.util.PGobject
import java.util.UUID

object ProjectsTable : UUIDTable("projects_copy") {
    val key = varchar("key", 20)
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
}

class ProjectEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<ProjectEntity>(ProjectsTable)

    var key by ProjectsTable.key
    var status by ProjectsTable.status
}