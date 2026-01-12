package com.quadro.data.models.tables

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime

object TaskAttachmentTable : Table("task_attachments") {
    val id = long("id").autoIncrement()
    val taskId = reference("task_id", TaskTable.id, ReferenceOption.CASCADE)
    val fileName = varchar("file_name", 255)
    val filePath = varchar("file_path", 500)
    val uploadedBy = reference("uploaded_by", UserTable.id, ReferenceOption.CASCADE)
    val uploadedAt = datetime("uploaded_at")

    override val primaryKey = PrimaryKey(id)
}