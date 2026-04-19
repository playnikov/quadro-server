package com.quadro.team.infrastructure.database.entities

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.javatime.timestampWithTimeZone
import java.util.UUID

object ProjectsTable : UUIDTable("projects_copy") {
    val companyId = uuid("company_id")
    val status = varchar("status", 50)
    val updatedAt = timestampWithTimeZone("updated_at")
}

class ProjectEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<ProjectEntity>(ProjectsTable)

    var companyId by ProjectsTable.companyId
    var status by ProjectsTable.status
    var updatedAt by ProjectsTable.updatedAt
}