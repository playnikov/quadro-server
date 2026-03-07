package com.quadro.datasource.entities

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.Instant
import java.util.*

object TasksTable : UUIDTable("tasks") {
    val projectId = uuid("project_id").references(ProjectsTable.id)
    val parentId = uuid("parent_id").references(id).nullable()
    val key = varchar("key", 20)
    val title = varchar("title", 500)
    val description = text("description").nullable()
    val type = varchar("type", 50)
    val status = varchar("status", 50)
    val priority = varchar("priority", 50)
    val resolution = varchar("resolution", 50).nullable()
    val assigneeId = uuid("assignee_id").nullable()
    val reporterId = uuid("reporter_id")
    val storyPoints = integer("story_points").nullable()
    val timeEstimate = long("time_estimate").nullable()
    val timeSpent = long("time_spent").default(0)
    val dueDate = timestamp("due_date").nullable()
    val startedAt = timestamp("started_at").nullable()
    val completedAt = timestamp("completed_at").nullable()
    val createdAt = timestamp("created_at").default(Instant.now())
    val updatedAt = timestamp("updated_at").default(Instant.now())
    val order = integer("order")
    val tags = text("tags").nullable()

    init {
        uniqueIndex(projectId, key)
    }
}

object TaskCommentsTable : UUIDTable("task_comments") {
    val taskId = uuid("task_id").references(TasksTable.id)
    val authorId = uuid("author_id").references(UsersTable.id)
    val content = text("content")
    val createdAt = timestamp("created_at").default(Instant.now())
    val updatedAt = timestamp("updated_at").default(Instant.now())
}

object TaskAttachmentsTable : UUIDTable("task_attachments") {
    val taskId = uuid("task_id").references(TasksTable.id)
    val uploadedBy = uuid("uploaded_by").references(UsersTable.id)
    val fileName = varchar("file_name", 500)
    val fileSize = long("file_size")
    val mimeType = varchar("mime_type", 100)
    val url = varchar("url", 1000)
    val createdAt = timestamp("created_at").default(Instant.now())
}

object TaskWatchersTable : UUIDTable("task_watchers") {
    val taskId = uuid("task_id").references(TasksTable.id)
    val userId = uuid("user_id").references(UsersTable.id)
    val addedAt = timestamp("added_at").default(Instant.now())

    init {
        uniqueIndex(taskId, userId)
    }
}

object TaskTimeLogsTable : UUIDTable("task_time_logs") {
    val taskId = uuid("task_id").references(TasksTable.id)
    val userId = uuid("user_id").references(UsersTable.id)
    val timeSpent = long("time_spent")
    val description = text("description").nullable()
    val loggedAt = timestamp("logged_at").default(Instant.now())
}

object TaskHistoryTable : UUIDTable("task_history") {
    val taskId = uuid("task_id").references(TasksTable.id)
    val userId = uuid("user_id")
    val field = varchar("field", 50)
    val oldValue = text("old_value").nullable()
    val newValue = text("new_value").nullable()
    val createdAt = timestamp("created_at").default(Instant.now())
}

class TaskEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<TaskEntity>(TasksTable)

    var projectId by TasksTable.projectId
    var parentId by TasksTable.parentId
    var key by TasksTable.key
    var title by TasksTable.title
    var description by TasksTable.description
    var type by TasksTable.type
    var status by TasksTable.status
    var priority by TasksTable.priority
    var resolution by TasksTable.resolution
    var assigneeId by TasksTable.assigneeId
    var reporterId by TasksTable.reporterId
    var storyPoints by TasksTable.storyPoints
    var timeEstimate by TasksTable.timeEstimate
    var timeSpent by TasksTable.timeSpent
    var dueDate by TasksTable.dueDate
    var startedAt by TasksTable.startedAt
    var completedAt by TasksTable.completedAt
    var createdAt by TasksTable.createdAt
    var updatedAt by TasksTable.updatedAt
    var order by TasksTable.order
    var tags by TasksTable.tags

}

class TaskCommentEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<TaskCommentEntity>(TaskCommentsTable)

    var taskId by TaskCommentsTable.taskId
    var authorId by TaskCommentsTable.authorId
    var content by TaskCommentsTable.content
    var createdAt by TaskCommentsTable.createdAt
    var updatedAt by TaskCommentsTable.updatedAt
}

class TaskAttachmentEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<TaskAttachmentEntity>(TaskAttachmentsTable)

    var taskId by TaskAttachmentsTable.taskId
    var uploadedBy by TaskAttachmentsTable.uploadedBy
    var fileName by TaskAttachmentsTable.fileName
    var fileSize by TaskAttachmentsTable.fileSize
    var mimeType by TaskAttachmentsTable.mimeType
    var url by TaskAttachmentsTable.url
    var createdAt by TaskAttachmentsTable.createdAt
}

class TaskWatcherEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<TaskWatcherEntity>(TaskWatchersTable)

    var taskId by TaskWatchersTable.taskId
    var userId by TaskWatchersTable.userId
    var addedAt by TaskWatchersTable.addedAt
}

class TaskTimeLogEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<TaskTimeLogEntity>(TaskTimeLogsTable)

    var taskId by TaskTimeLogsTable.taskId
    var userId by TaskTimeLogsTable.userId
    var timeSpent by TaskTimeLogsTable.timeSpent
    var description by TaskTimeLogsTable.description
    var loggedAt by TaskTimeLogsTable.loggedAt
}

class TaskHistoryEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<TaskHistoryEntity>(TaskHistoryTable)

    var taskId by TaskHistoryTable.taskId
    var userId by TaskHistoryTable.userId
    var field by TaskHistoryTable.field
    var oldValue by TaskHistoryTable.oldValue
    var newValue by TaskHistoryTable.newValue
    var createdAt by TaskHistoryTable.createdAt
}