package com.quadro.datasource.entities

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.Instant
import java.util.UUID

enum class TeamStatusDb {
    ACTIVE, ARCHIVED, DISBANDED
}

enum class TeamVisibilityDb {
    PUBLIC, PRIVATE, HIDDEN
}

enum class TeamRoleDb {
    LEAD, ADMIN, MEMBER, GUEST
}

object TeamsTable : UUIDTable("teams") {
    val companyId = uuid("company_id").references(CompaniesTable.id)
    val name = varchar("name", 255)
    val description = text("description").nullable()
    val avatar = varchar("avatar", 500).nullable()
    val settings = text("settings")
    val status = enumerationByName("status", 50, TeamStatusDb::class).default(TeamStatusDb.ACTIVE)
    val visibility = enumerationByName("visibility", 50, TeamVisibilityDb::class).default(TeamVisibilityDb.PRIVATE)
    val leadId = uuid("lead_id").references(UsersTable.id)
    val createdAt = timestamp("created_at").default(Instant.now())
    val updatedAt = timestamp("updated_at").default(Instant.now())
    val archivedAt = timestamp("archived_at").nullable()
    val currentMembers = integer("current_members").default(0)

    init {
        uniqueIndex(companyId, name)
    }
}

object TeamMembersTable : UUIDTable("team_members") {
    val teamId = uuid("team_id").references(TeamsTable.id)
    val userId = uuid("user_id").references(UsersTable.id)
    val role = enumerationByName("role", 50, TeamRoleDb::class).default(TeamRoleDb.MEMBER)
    val joinedAt  = timestamp("joined_at").default(Instant.now())
    val invitedBy = uuid("invited_by").references(UsersTable.id)
    val invitedAt  = timestamp("invited_at").default(Instant.now())
    val isActive = bool("is_active").default(true)

    init {
        uniqueIndex(teamId, userId)
    }
}

class TeamEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<TeamEntity>(TeamsTable)

    var companyId by TeamsTable.companyId
    var name by TeamsTable.name
    var description by TeamsTable.description
    var avatar by TeamsTable.avatar
    var settings by TeamsTable.settings
    var status by TeamsTable.status
    var visibility by TeamsTable.visibility
    var leadId by TeamsTable.leadId
    var createdAt by TeamsTable.createdAt
    var updatedAt by TeamsTable.updatedAt
    var archivedAt by TeamsTable.archivedAt
    var currentMembers by TeamsTable.currentMembers
}

class TeamMemberEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<TeamMemberEntity>(TeamMembersTable)

    var teamId by TeamMembersTable.teamId
    var userId by TeamMembersTable.userId
    var role by TeamMembersTable.role
    var joinedAt by TeamMembersTable.joinedAt
    var invitedBy by TeamMembersTable.invitedBy
    var invitedAt by TeamMembersTable.invitedAt
    var isActive by TeamMembersTable.isActive
}