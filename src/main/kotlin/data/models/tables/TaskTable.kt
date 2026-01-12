package com.quadro.data.models.tables

import com.quadro.data.models.TaskPriority
import com.quadro.data.models.TaskStatus
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime

object TaskTable : Table("tasks") {
    val id = long("id").autoIncrement()
    val title = varchar("title", 255)
    val description = text("description").nullable()
    val status = customEnumeration(
        "status",
        "task_status",
        { value -> TaskStatus.valueOf(value as String) },
        { it -> it.name }
    ).default(TaskStatus.TODO)
    val priority = customEnumeration(
        "priority",
        "task_priority",
        { value -> TaskPriority.valueOf(value as String) },
        { it -> it.name }
    ).default(TaskPriority.MEDIUM)
    val dueDate = datetime("due_date").nullable()
    val projectId = reference("project_id", ProjectTable.id, ReferenceOption.CASCADE)
    val assigneeId = reference("assignee_id", UserTable.id, ReferenceOption.SET_NULL)
    val reporterId = reference("reporter_id", UserTable.id, ReferenceOption.CASCADE)
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")

    override val primaryKey = PrimaryKey(ProjectTable.id)
}