package com.quadro.data.models.tables

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime

object TaskCommentTable : Table("task_comments") {
    val id = long("id").autoIncrement()
    val taskId = reference("task_id", id, ReferenceOption.CASCADE)
    val authorId = reference("author_id", UserTable.id, ReferenceOption.CASCADE)
    val content = varchar("content", 500)
    val createdAt = datetime("created_at")

    override val primaryKey = PrimaryKey(id)
}