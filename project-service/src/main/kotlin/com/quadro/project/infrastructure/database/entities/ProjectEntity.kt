package com.quadro.project.infrastructure.database.entities

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.javatime.timestampWithTimeZone
import java.time.Instant
import java.util.UUID

object ProjectsTable : UUIDTable("projects") {
    val companyId = uuid("company_id")
    val type = varchar("type", 50)
    val name = varchar("name", 50)
    val key = varchar("key", 20)
    val description = text("description").nullable()
    val status = varchar("status", 50)
    val priority = varchar("priority", 50)
    val visibility = varchar("visibility", 50)
    val leadId = uuid("lead_id")
    val ownerId = uuid("owner_id")
    val startDate = timestampWithTimeZone("start_date").nullable()
    val endDate = timestampWithTimeZone("end_date").nullable()
    val completedAt = timestampWithTimeZone("completed_at").nullable()
    val createdAt = timestampWithTimeZone("created_at")
    val updatedAt = timestampWithTimeZone("updated_at")
}

class ProjectEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<ProjectEntity>(ProjectsTable)

    var companyId by ProjectsTable.companyId
    var type by ProjectsTable.type
    var name by ProjectsTable.name
    var key by ProjectsTable.key
    var description by ProjectsTable.description
    var status by ProjectsTable.status
    var priority by ProjectsTable.priority
    var visibility by ProjectsTable.visibility
    var leadId by ProjectsTable.leadId
    var ownerId by ProjectsTable.ownerId
    var startDate by ProjectsTable.startDate
    var endDate by ProjectsTable.endDate
    var completedAt by ProjectsTable.completedAt
    var createdAt by ProjectsTable.createdAt
    var updatedAt by ProjectsTable.updatedAt
}