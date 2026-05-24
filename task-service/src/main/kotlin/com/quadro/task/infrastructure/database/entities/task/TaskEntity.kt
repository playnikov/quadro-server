package com.quadro.task.infrastructure.database.entities.task

import com.quadro.task.domain.models.task.SprintStatus
import com.quadro.task.domain.models.task.TaskPriority
import com.quadro.task.domain.models.task.TaskStatus
import com.quadro.task.domain.models.task.TaskType
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.javatime.timestampWithTimeZone
import org.postgresql.util.PGobject
import java.util.UUID

object TasksTable : UUIDTable("tasks") {
    val projectId = uuid("project_id")
    val sprintId = uuid("sprint_id").nullable()
    val parentTaskId = uuid("parent_task_id").nullable()
    val number = integer("number")
    val title = varchar("title", 200)
    val description = text("description").nullable()
    val status = customEnumeration(
        name = "status",
        sql = "task_status",
        fromDb = { value -> TaskStatus.valueOf(value as String) },
        toDb = { status ->
            PGobject().apply {
                type = "task_status"
                value = status.name
            }
        }
    )
    val priority = customEnumeration(
        name = "priority",
        sql = "task_priority",
        fromDb = { value -> TaskPriority.valueOf(value as String) },
        toDb = { priority ->
            PGobject().apply {
                type = "task_priority"
                value = priority.name
            }
        }
    )
    val type = customEnumeration(
        name = "type",
        sql = "task_type",
        fromDb = { value -> TaskType.valueOf(value as String) },
        toDb = { typeTask ->
            PGobject().apply {
                type = "task_type"
                value = typeTask.name
            }
        }
    )
    val assigneeId = uuid("assignee_id").nullable()
    val reporterId = uuid("reporter_id")
    val storyPoints = integer("story_points").nullable()
    val estimatedHours = double("estimated_hours").nullable()
    val loggedHours = double("logged_hours").nullable()
    val dueDate = timestampWithTimeZone("due_date").nullable()
    val startedAt = timestampWithTimeZone("started_at").nullable()
    val completedAt = timestampWithTimeZone("completed_at").nullable()
    val labels = array<String>("labels").nullable()
    val createdAt = timestampWithTimeZone("created_at")
    val updatedAt = timestampWithTimeZone("updated_at")
}

class TaskEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<TaskEntity>(TasksTable)

    var projectId by TasksTable.projectId
    var sprintId by TasksTable.sprintId
    var parentTaskId by TasksTable.parentTaskId
    var number by TasksTable.number
    var title by TasksTable.title
    var description by TasksTable.description
    var status by TasksTable.status
    var priority by TasksTable.priority
    var type by TasksTable.type
    var assigneeId by TasksTable.assigneeId
    var reporterId by TasksTable.reporterId
    var storyPoints by TasksTable.storyPoints
    var estimatedHours by TasksTable.estimatedHours
    var loggedHours by TasksTable.loggedHours
    var dueDate by TasksTable.dueDate
    var startedAt by TasksTable.startedAt
    var completedAt by TasksTable.completedAt
    var labels by TasksTable.labels
    var createdAt by TasksTable.createdAt
    var updatedAt by TasksTable.updatedAt
}