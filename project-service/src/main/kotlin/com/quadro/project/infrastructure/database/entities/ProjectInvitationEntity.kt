package com.quadro.project.infrastructure.database.entities

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.javatime.timestampWithTimeZone
import java.util.UUID

object ProjectInvitationsTable : UUIDTable("company_invitations") {
    val projectId = uuid("project_id").references(ProjectsTable.id)
    val invitedBy = uuid("invited_by")
    val inviteType = varchar("invite_type", 50)
    val identifier = varchar("identifier", 255)
    val role = varchar("role", 50)
    val status = varchar("status", 50)
    val token = varchar("token", 500).uniqueIndex()
    val expiresAt = timestampWithTimeZone("expires_at")
    val createdAt = timestampWithTimeZone("created_at")
    val acceptedAt = timestampWithTimeZone("accepted_at").nullable()
    val acceptedBy = uuid("accepted_by").nullable()
    val message = text("message").nullable()
}

class ProjectInvitationEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<ProjectInvitationEntity>(ProjectInvitationsTable)

    var projectId by ProjectMembersTable.projectId
    var invitedBy by ProjectInvitationsTable.invitedBy
    var inviteType by ProjectInvitationsTable.inviteType
    var identifier by ProjectInvitationsTable.identifier
    var role by ProjectInvitationsTable.role
    var status by ProjectInvitationsTable.status
    var token by ProjectInvitationsTable.token
    var expiresAt by ProjectInvitationsTable.expiresAt
    var createdAt by ProjectInvitationsTable.createdAt
    var acceptedAt by ProjectInvitationsTable.acceptedAt
    var acceptedBy by ProjectInvitationsTable.acceptedBy
    var message by ProjectInvitationsTable.message
}