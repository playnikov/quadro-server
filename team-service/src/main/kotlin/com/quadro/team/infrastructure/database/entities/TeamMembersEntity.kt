package com.quadro.team.infrastructure.database.entities

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.javatime.timestampWithTimeZone
import java.util.UUID

object TeamMembersTable : UUIDTable("team_members") {
    val teamId = uuid("team_id").references(TeamsTable.id)
    val userId = uuid("user_id")
    val role = varchar("role", 50)
    val joinedAt = timestampWithTimeZone("joined_at").nullable()
    val invitedBy = uuid("invited_by")
    val invitedAt = timestampWithTimeZone("invited_at").nullable()
    val lastActiveAt = timestampWithTimeZone("last_active_at").nullable()
    val isActive = bool("is_active").default(false)
}

class TeamMembersEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<TeamMembersEntity>(TeamMembersTable)

    var teamId by TeamMembersTable.teamId
    var userId by TeamMembersTable.userId
    var role by TeamMembersTable.role
    var joinedAt by TeamMembersTable.joinedAt
    var invitedBy by TeamMembersTable.invitedBy
    var invitedAt by TeamMembersTable.invitedAt
    var lastActiveAt by TeamMembersTable.lastActiveAt
    var isActive by TeamMembersTable.isActive
}