package com.quadro.task.infrastructure.database.entities.task

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.javatime.timestampWithTimeZone
import java.util.UUID

object TaskHistoryTable : UUIDTable("task_history") {
    val taskId = uuid("task_id").references(TasksTable.id)
    val userId = uuid("user_id")
    val action = varchar("action", 50)
    val oldValue = text("old_value").nullable()
    val newValue = text("new_value").nullable()
    val createdAt = timestampWithTimeZone("created_at")
}

class TaskHistoryEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<TaskHistoryEntity>(TaskHistoryTable)

    var taskId by TaskHistoryTable.taskId
    var userId by TaskHistoryTable.userId
    var action by TaskHistoryTable.action
    var oldValue by TaskHistoryTable.oldValue
    var newValue by TaskHistoryTable.newValue
    var createdAt by TaskHistoryTable.createdAt
}