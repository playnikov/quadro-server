package com.quadro.task.infrastructure.database.entities.task

import com.quadro.task.domain.models.task.HistoryAction
import com.quadro.task.domain.models.task.TaskType
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.javatime.timestampWithTimeZone
import org.postgresql.util.PGobject
import java.util.UUID

object TaskHistoryTable : UUIDTable("task_history") {
    val taskId = uuid("task_id").references(TasksTable.id)
    val userId = uuid("user_id")
    val action = customEnumeration(
        name = "action",
        sql = "history_action",
        fromDb = { value -> HistoryAction.valueOf(value as String) },
        toDb = { action ->
            PGobject().apply {
                type = "history_action"
                value = action.name
            }
        }
    )
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