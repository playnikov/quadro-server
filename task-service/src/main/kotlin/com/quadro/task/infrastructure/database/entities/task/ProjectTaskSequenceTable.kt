package com.quadro.task.infrastructure.database.entities.task

import com.quadro.task.infrastructure.database.entities.UsersTable.integer
import com.quadro.task.infrastructure.database.entities.project.ProjectsTable
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import java.util.UUID

object ProjectTaskSequenceTable : UUIDTable("project_task_sequence") {
    val projectId = uuid("project_id").references(ProjectsTable.id).uniqueIndex()
    val lastNumber = integer("last_number").default(0)
}

class ProjectTaskSequenceEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<ProjectTaskSequenceEntity>(ProjectTaskSequenceTable)
    var projectId by ProjectTaskSequenceTable.projectId
    var lastNumber by ProjectTaskSequenceTable.lastNumber
}