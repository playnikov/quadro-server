package com.quadro.task.infrastructure.database.entities.team

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.javatime.timestampWithTimeZone
import java.util.UUID

object TeamMembersTable : UUIDTable("team_members_copy") {
    val teamId = uuid("team_id").references(TeamsTable.id)
    val userId = uuid("user_id")
    val role = varchar("role", 50)
    val isActive = bool("is_active").default(false)
}

class TeamMembersEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<TeamMembersEntity>(TeamMembersTable)

    var teamId by TeamMembersTable.teamId
    var userId by TeamMembersTable.userId
    var role by TeamMembersTable.role
    var isActive by TeamMembersTable.isActive
}