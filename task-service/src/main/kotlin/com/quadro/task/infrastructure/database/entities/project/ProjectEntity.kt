package com.quadro.task.infrastructure.database.entities.project

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import java.util.UUID

object ProjectsTable : UUIDTable("projects_copy") {
    val key = varchar("key", 20)
    val status = varchar("status", 50)
}

class ProjectEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<ProjectEntity>(ProjectsTable)

    var key by ProjectsTable.key
    var status by ProjectsTable.status
}