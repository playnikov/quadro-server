package com.quadro.team.infrastructure.database.entities

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.javatime.timestampWithTimeZone
import java.util.UUID

object TeamsTable : UUIDTable("teams") {
    val companyId = uuid("company_id")
    val name = varchar("name", 50)
    val description = text("description").nullable()
    val avatar = varchar("avatar", 500).nullable()
    val status = varchar("status", 50)
    val visibility = varchar("visibility", 50)
    val leadId = uuid("lead_id").nullable()
    val createdBy = uuid("created_by")
    val createdAt = timestampWithTimeZone("created_at")
    val updatedAt = timestampWithTimeZone("updated_at")
}

class TeamEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<TeamEntity>(TeamsTable)

    var companyId by TeamsTable.companyId
    var name by TeamsTable.name
    var description by TeamsTable.description
    var avatar by TeamsTable.avatar
    var status by TeamsTable.status
    var visibility by TeamsTable.visibility
    var leadId by TeamsTable.leadId
    var createdBy by TeamsTable.createdBy
    var createdAt by TeamsTable.createdAt
    var updatedAt by TeamsTable.updatedAt
}