package com.quadro.datasource.entities

import com.quadro.domain.models.CompanyRole
import com.quadro.domain.models.CompanyStatus
import com.quadro.domain.models.InvitationStatus
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

    val allowGuestAccess = bool("allow_guest_access").default(false)
    val requireEmailVerification = bool("require_email_verification").default(true)
    val defaultUserRole = enumerationByName("default_user_role", 50, CompanyRole::class).default(CompanyRole.MEMBER)
    val projectCreationRole = enumerationByName("project_creation_role", 50, CompanyRole::class).default(CompanyRole.MANAGER)
    val teamCreationRole = enumerationByName("team_creation_role", 50, CompanyRole::class).default(CompanyRole.MANAGER)
    val invitationExpiryDays = integer("invitation_expiry_days").default(7)
    val maxTeamsPerProject = integer("max_teams_per_project").default(10)
    val maxUsersPerTeam = integer("max_users_per_team").default(50)

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
}

object CompanyInvitationsTable : UUIDTable("company_invitations") {
    val companyId = uuid("company_id").references(CompaniesTable.id)
    val invitedBy = uuid("invited_by").references(UsersTable.id)
    val identifier = varchar("identifier", 255)
    val role = enumerationByName("role", 50, CompanyRole::class)
    val status = enumerationByName("status", 50, InvitationStatus::class)
    val token = varchar("token", 255).nullable().uniqueIndex()
    val expiresAt = timestamp("expires_at")
    val createdAt = timestamp("created_at").default(Instant.now())
    val acceptedAt = timestamp("accepted_at").nullable()
    val acceptedBy = uuid("accepted_by").nullable()
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

    var allowGuestAccess by CompaniesTable.allowGuestAccess
    var requireEmailVerification by CompaniesTable.requireEmailVerification
    var defaultUserRole by CompaniesTable.defaultUserRole
    var projectCreationRole by CompaniesTable.projectCreationRole
    var teamCreationRole by CompaniesTable.teamCreationRole
    var invitationExpiryDays by CompaniesTable.invitationExpiryDays
    var maxTeamsPerProject by CompaniesTable.maxTeamsPerProject
    var maxUsersPerTeam by CompaniesTable.maxUsersPerTeam


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