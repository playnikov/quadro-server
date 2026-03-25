package com.quadro.company.infrastructure.database.entities

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.javatime.timestampWithTimeZone
import java.util.UUID

object CompanyInvitationsTable : UUIDTable("company_invitations") {
    val companyId = uuid("company_id").references(CompaniesTable.id)
    val teamId = uuid("team_id").nullable()
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

class CompanyInvitationEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<CompanyInvitationEntity>(CompanyInvitationsTable)

    var companyId by CompanyInvitationsTable.companyId
    var teamId by CompanyInvitationsTable.teamId
    var invitedBy by CompanyInvitationsTable.invitedBy
    var inviteType by CompanyInvitationsTable.inviteType
    var identifier by CompanyInvitationsTable.identifier
    var role by CompanyInvitationsTable.role
    var status by CompanyInvitationsTable.status
    var token by CompanyInvitationsTable.token
    var expiresAt by CompanyInvitationsTable.expiresAt
    var createdAt by CompanyInvitationsTable.createdAt
    var acceptedAt by CompanyInvitationsTable.acceptedAt
    var acceptedBy by CompanyInvitationsTable.acceptedBy
    var message by CompanyInvitationsTable.message
}