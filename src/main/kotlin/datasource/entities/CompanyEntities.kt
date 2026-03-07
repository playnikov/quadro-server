package com.quadro.datasource.entities

import com.quadro.domain.models.company.CompanyRole
import com.quadro.domain.models.company.CompanyStatus
import com.quadro.domain.models.company.InvitationStatus
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.Instant
import java.util.UUID

object CompaniesTable : UUIDTable("companies") {
    val name = varchar("name", 255)
    val description = text("description").nullable()
    val logo = varchar("logo", 500).nullable()
    val website = varchar("website", 255).nullable()
    val email = varchar("email", 255).nullable()
    val phone = varchar("phone", 50).nullable()
    val address = text("address").nullable()
    val taxId = varchar("tax_id", 50).nullable()
    val status = enumerationByName("status", 50, CompanyStatus::class)
    val ownerId = uuid("owner_id").references(UsersTable.id)
    val settings = text("settings")

    val createdAt = timestamp("created_at").default(Instant.now())
    val updatedAt = timestamp("updated_at").default(Instant.now())
    val deletedAt = timestamp("deleted_at").nullable()
}

object CompanyMembersTable : UUIDTable("company_members") {
    val companyId = uuid("company_id").references(CompaniesTable.id)
    val userId = uuid("user_id").references(UsersTable.id)
    val role = enumerationByName("role", 50, CompanyRole::class)
    val joinedAt = timestamp("joined_at").default(Instant.now())
    val invitedBy = uuid("invited_by").references(UsersTable.id)
    val invitedAt = timestamp("invited_at").default(Instant.now())
    val isActive = bool("is_active").default(true)

    init {
        uniqueIndex(companyId, userId)
    }
}

object CompanyInvitationsTable : UUIDTable("company_invitations") {
    val companyId = uuid("company_id").references(CompaniesTable.id)
    val invitedBy = uuid("invited_by").references(UsersTable.id)
    val teamId = uuid("team_id").references(TeamsTable.id).nullable()
    val role = enumerationByName("role", 50, CompanyRole::class)
    val status = enumerationByName("status", 50, InvitationStatus::class)
    val token = varchar("token", 500).uniqueIndex()
    val expiresAt = timestamp("expires_at")
    val createdAt = timestamp("created_at").default(Instant.now())
    val acceptedAt = timestamp("accepted_at").nullable()
    val acceptedBy = uuid("accepted_by").references(UsersTable.id).nullable()
    val message = text("message").nullable()
}

class CompanyEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<CompanyEntity>(CompaniesTable)

    var name by CompaniesTable.name
    var description by CompaniesTable.description
    var logo by CompaniesTable.logo
    var website by CompaniesTable.website
    var email by CompaniesTable.email
    var phone by CompaniesTable.phone
    var address by CompaniesTable.address
    var taxId by CompaniesTable.taxId
    var status by CompaniesTable.status
    var ownerId by CompaniesTable.ownerId
    var settings by CompaniesTable.settings
    var createdAt by CompaniesTable.createdAt
    var updatedAt by CompaniesTable.updatedAt
    var deletedAt by CompaniesTable.deletedAt
}

class CompanyMemberEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<CompanyMemberEntity>(CompanyMembersTable)

    var companyId by CompanyMembersTable.companyId
    var userId by CompanyMembersTable.userId
    var role by CompanyMembersTable.role
    var joinedAt by CompanyMembersTable.joinedAt
    var invitedBy by CompanyMembersTable.invitedBy
    var invitedAt by CompanyMembersTable.invitedAt
    var isActive by CompanyMembersTable.isActive
}

class CompanyInvitationEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<CompanyInvitationEntity>(CompanyInvitationsTable)

    var companyId by CompanyInvitationsTable.companyId
    var invitedBy by CompanyInvitationsTable.invitedBy
    var teamId by CompanyInvitationsTable.teamId
    var role by CompanyInvitationsTable.role
    var status by CompanyInvitationsTable.status
    var token by CompanyInvitationsTable.token
    var expiresAt by CompanyInvitationsTable.expiresAt
    var createdAt by CompanyInvitationsTable.createdAt
    var acceptedAt by CompanyInvitationsTable.acceptedAt
    var acceptedBy by CompanyInvitationsTable.acceptedBy
    var message by CompanyInvitationsTable.message
}