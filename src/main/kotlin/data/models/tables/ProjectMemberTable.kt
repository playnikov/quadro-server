package com.quadro.data.models.tables

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime

object ProjectMemberTable : Table("project_members") {
    val id = long("id").autoIncrement()
    val projectId = reference("project_id", ProjectTable.id, ReferenceOption.CASCADE)
    val userId = reference("user_id", UserTable.id, ReferenceOption.CASCADE)
    val joinedAt = datetime("joined_at")

    override val primaryKey = PrimaryKey(id)
}