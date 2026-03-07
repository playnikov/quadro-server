package com.quadro.datasource.entities

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.Instant
import java.util.UUID

object ProjectsTable : UUIDTable("projects") {
    val companyId = uuid("company_id").references(CompaniesTable.id)
    val type = varchar("type", 50)
    val name = varchar("name", 255)
    val key = varchar("key", 10)
    val description = text("description").nullable()
    val status = varchar("status", 50)
    val priority = varchar("priority", 50)
    val visibility = varchar("visibility", 50)
    val leadId = uuid("lead_id")
    val ownerId = uuid("owner_id")
    val settings = text("settings")
    val startDate = timestamp("start_date").nullable()
    val endDate = timestamp("end_date").nullable()
    val completedAt = timestamp("completed_at").nullable()
    val createdAt = timestamp("created_at").default(Instant.now())
    val updatedAt = timestamp("updated_at").default(Instant.now())
    val archivedAt = timestamp("archived_at").nullable()

    init {
        uniqueIndex(companyId, key)
        uniqueIndex(companyId, name)
    }
}

object ProjectTeamsTable : UUIDTable("project_teams") {
    val projectId = uuid("project_id").references(ProjectsTable.id)
    val teamId = uuid("team_id").references(TeamsTable.id)
    val role = varchar("role", 50)
    val isLeadTeam = bool("is_lead_team").default(false)
    val assignedAt = timestamp("assigned_at").default(Instant.now())
    val assignedBy = uuid("assigned_by").references(UsersTable.id)

    init {
        uniqueIndex(projectId, teamId)
    }
}

object ProjectMembersTable : UUIDTable("project_members") {
    val projectId = uuid("project_id").references(ProjectsTable.id)
    val userId = uuid("user_id").references(UsersTable.id)
    val role = varchar("role", 50)
    val joinedAt = timestamp("joined_at").default(Instant.now())
    val invitedBy = uuid("invited_by").references(UsersTable.id)
    val invitedAt = timestamp("invited_at").default(Instant.now())
    val sourceTeamId = uuid("source_team_id").references(TeamsTable.id).nullable()

    init {
        uniqueIndex(projectId, userId)
    }
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
    var settings by ProjectsTable.settings
    var startDate by ProjectsTable.startDate
    var endDate by ProjectsTable.endDate
    var completedAt by ProjectsTable.completedAt
    var createdAt by ProjectsTable.createdAt
    var updatedAt by ProjectsTable.updatedAt
    var archivedAt by ProjectsTable.archivedAt
}

class ProjectTeamEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<ProjectTeamEntity>(ProjectTeamsTable)

    var projectId by ProjectTeamsTable.projectId
    var teamId by ProjectTeamsTable.teamId
    var role by ProjectTeamsTable.role
    var isLeadTeam by ProjectTeamsTable.isLeadTeam
    var assignedAt by ProjectTeamsTable.assignedAt
    var assignedBy by ProjectTeamsTable.assignedBy
}

class ProjectMemberEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<ProjectMemberEntity>(ProjectMembersTable)

    var projectId by ProjectMembersTable.projectId
    var userId by ProjectMembersTable.userId
    var role by ProjectMembersTable.role
    var joinedAt by ProjectMembersTable.joinedAt
    var invitedBy by ProjectMembersTable.invitedBy
    var invitedAt by ProjectMembersTable.invitedAt
    var sourceTeamId by ProjectMembersTable.sourceTeamId
}