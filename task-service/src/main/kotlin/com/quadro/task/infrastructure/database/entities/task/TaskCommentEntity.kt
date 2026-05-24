package com.quadro.task.infrastructure.database.entities.task

import com.quadro.task.infrastructure.database.entities.UsersTable
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ColumnType
import org.jetbrains.exposed.sql.javatime.timestampWithTimeZone
import java.util.UUID

object TaskCommentsTable : UUIDTable("task_comments") {
    val taskId = uuid("task_id").references(TasksTable.id)
    val authorId = uuid("author_id").references(UsersTable.id)
    val content = text("content")
    val parentId = uuid("parent_id").nullable()
    val isEdited = bool("is_edited").default(false)
    val isDeleted = bool("is_deleted").default(false)
    val mentions = array<UUID>("mentions").nullable()
    val createdAt = timestampWithTimeZone("created_at")
    val updatedAt = timestampWithTimeZone("updated_at")
}

class TaskCommentEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<TaskCommentEntity>(TaskCommentsTable)

    var taskId by TaskCommentsTable.taskId
    var authorId by TaskCommentsTable.authorId
    var content by TaskCommentsTable.content
    var parentId by TaskCommentsTable.parentId
    var isEdited by TaskCommentsTable.isEdited
    var isDeleted by TaskCommentsTable.isDeleted
    var mentions by TaskCommentsTable.mentions
    var createdAt by TaskCommentsTable.createdAt
    var updatedAt by TaskCommentsTable.updatedAt
}