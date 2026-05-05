package com.quadro.task.infrastructure.database.entities.task

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.javatime.timestampWithTimeZone
import java.util.UUID

object TaskAttachmentsTable : UUIDTable("task_attachments") {
    val taskId = uuid("task_id").references(TasksTable.id)
    val uploadedBy = uuid("uploaded_by")
    val fileName = varchar("file_name", 255)
    val fileSize = long("file_size")
    val url = varchar("url", 500)
    val createdAt = timestampWithTimeZone("created_at")
}

class TaskAttachmentEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<TaskAttachmentEntity>(TaskAttachmentsTable)

    var taskId by TaskAttachmentsTable.taskId
    var uploadedBy by TaskAttachmentsTable.uploadedBy
    var fileName by TaskAttachmentsTable.fileName
    var fileSize by TaskAttachmentsTable.fileSize
    var fileUrl by TaskAttachmentsTable.url
    var createdAt by TaskAttachmentsTable.createdAt
}