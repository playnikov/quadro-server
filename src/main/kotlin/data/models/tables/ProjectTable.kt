package com.quadro.data.models.tables

import com.quadro.data.models.ProjectStatus
import com.quadro.data.models.ProjectType
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime

object ProjectTable : Table("projects") {
    val id = long("id").autoIncrement()
    val key = varchar("key", 10).uniqueIndex()
    val title = varchar("title", 255)
    val description = text("description").nullable()
    val createdBy = reference("created_by", UserTable.id, onDelete = ReferenceOption.CASCADE)
    val status = customEnumeration(
        "status",
        "project_status",
        { value -> ProjectStatus.valueOf(value as String) },
        { it -> it.name }
    ).default(ProjectStatus.ACTIVE)
    val projectType = customEnumeration(
        "project_type",
        "project_type",
        { value -> ProjectType.valueOf(value as String) },
        { it -> it.name }
    ).default(ProjectType.INTERNAL)
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")

    override val primaryKey = PrimaryKey(id)
}