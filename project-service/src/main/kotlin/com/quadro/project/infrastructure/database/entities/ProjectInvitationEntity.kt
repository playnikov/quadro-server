package com.quadro.project.infrastructure.database.entities

import com.quadro.project.domain.models.InviteStatus
import com.quadro.project.domain.models.InviteType
import com.quadro.project.domain.models.MemberRole
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.javatime.timestampWithTimeZone
import org.postgresql.util.PGobject
import java.util.UUID

object ProjectInvitationsTable : UUIDTable("project_invitations") {
    val projectId = uuid("project_id").references(ProjectsTable.id)
    val invitedBy = uuid("invited_by")
    val type = customEnumeration(
        name = "type",
        sql = "invite_type",
        fromDb = { value -> InviteType.valueOf(value as String) },
        toDb = { inviteType ->
            PGobject().apply {
                type = "invite_type"
                value = inviteType.name
            }
        }
    )
    val identifier = varchar("identifier", 255)
    val role = customEnumeration(
        name = "role",
        sql = "member_roles",
        fromDb = { value -> MemberRole.valueOf(value as String) },
        toDb = { role ->
            PGobject().apply {
                type = "member_roles"
                value = role.name
            }
        }
    )
    val status = customEnumeration(
        name = "status",
        sql = "invite_status",
        fromDb = { value -> InviteStatus.valueOf(value as String) },
        toDb = { status ->
            PGobject().apply {
                type = "invite_status"
                value = status.name
            }
        }
    )
    val token = varchar("token", 500).uniqueIndex()
    val expiresAt = timestampWithTimeZone("expires_at")
    val createdAt = timestampWithTimeZone("created_at")
    val acceptedAt = timestampWithTimeZone("accepted_at").nullable()
    val acceptedBy = uuid("accepted_by").nullable()
    val message = text("message").nullable()
}

class ProjectInvitationEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<ProjectInvitationEntity>(ProjectInvitationsTable)

    var projectId by ProjectInvitationsTable.projectId
    var invitedBy by ProjectInvitationsTable.invitedBy
    var type by ProjectInvitationsTable.type
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