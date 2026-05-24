package com.quadro.task.infrastructure.database.entities.task

import com.quadro.task.domain.models.project.ProjectStatus
import com.quadro.task.domain.models.task.SprintStatus
import com.quadro.task.infrastructure.database.entities.UsersTable
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.javatime.timestampWithTimeZone
import org.postgresql.util.PGobject
import java.util.UUID

object SprintsTable : UUIDTable("task_sprint") {
    val projectId = uuid("project_id")
    val name = varchar("name", 255)
    val goal = text("goal").nullable()
    val status = customEnumeration(
        name = "status",
        sql = "sprint_status",
        fromDb = { value -> SprintStatus.valueOf(value as String) },
        toDb = { status ->
            PGobject().apply {
                type = "sprint_status"
                value = status.name
            }
        }
    )
    val startDate = timestampWithTimeZone("start_date")
    val endDate = timestampWithTimeZone("end_date")
    val createdBy = uuid("created_by").references(UsersTable.id)
    val createdAt = timestampWithTimeZone("created_at")
    val updatedAt = timestampWithTimeZone("updated_at")
}

class SprintEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<SprintEntity>(SprintsTable)

    var projectId by SprintsTable.projectId
    var name by SprintsTable.name
    var goal by SprintsTable.goal
    var status by SprintsTable.status
    var startDate by SprintsTable.startDate
    var endDate by SprintsTable.endDate
    var createdBy by SprintsTable.createdBy
    var createdAt by SprintsTable.createdAt
    var updatedAt by SprintsTable.updatedAt
}