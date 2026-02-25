package com.quadro.datasource.mappers

import com.quadro.datasource.entities.CompaniesTable
import com.quadro.datasource.entities.CompanyEntity
import com.quadro.datasource.entities.CompanyInvitationEntity
import com.quadro.datasource.entities.CompanyMemberEntity
import com.quadro.domain.models.Company
import com.quadro.domain.models.CompanyInvitation
import com.quadro.domain.models.CompanyMember
import com.quadro.domain.models.CompanySettings
import java.time.Instant

object CompanyMapper  {
    fun toDomain(entity: CompanyEntity): Company = Company(
        id = entity.id.value,
        name = entity.name,
        description = entity.description,
        logo = entity.logo,
        website = entity.website,
        email = entity.email,
        phone = entity.phone,
        address = entity.address,
        taxId = entity.taxId,
        companyStatus = entity.status,
        ownerId = entity.ownerId,
        companySettings = CompanySettings(
            allowGuestAccess = entity.allowGuestAccess,
            requireEmailVerification = entity.requireEmailVerification,
            defaultUserRole = entity.defaultUserRole,
            projectCreationRole = entity.projectCreationRole,
            teamCreationRole = entity.teamCreationRole,
            invitationExpiryDays = entity.invitationExpiryDays,
            maxTeamsPerProject = entity.maxTeamsPerProject,
            maxUsersPerTeam = entity.maxUsersPerTeam
        ),
        createdAt = entity.createdAt.toEpochMilli(),
        updatedAt = entity.updatedAt.toEpochMilli(),
        deletedAt = entity.updatedAt.toEpochMilli()
    )

    fun toEntity(domain: Company): CompanyEntity = CompanyEntity.findById(domain.id) ?: CompanyEntity.new(domain.id) {
        applyDomainToEntity(this, domain)
    }

    fun updateEntity(entity: CompanyEntity, domain: Company) {
        applyDomainToEntity(entity, domain)
    }

    private fun applyDomainToEntity(entity: CompanyEntity, domain: Company) {
        entity.name = domain.name
        entity.description = domain.description
        entity.logo = domain.logo
        entity.website = domain.website
        entity.email = domain.email
        entity.phone = domain.phone
        entity.address = domain.address
        entity.taxId = domain.taxId
        entity.status = domain.companyStatus
        entity.ownerId = domain.ownerId
        entity.createdAt = Instant.ofEpochMilli(domain.createdAt)
        entity.updatedAt = Instant.ofEpochMilli(domain.updatedAt)
        entity.deletedAt = domain.deletedAt?.let { Instant.ofEpochMilli(it) }

        entity.allowGuestAccess = domain.companySettings.allowGuestAccess
        entity.requireEmailVerification = domain.companySettings.requireEmailVerification
        entity.defaultUserRole = domain.companySettings.defaultUserRole
        entity.projectCreationRole = domain.companySettings.projectCreationRole
        entity.teamCreationRole = domain.companySettings.teamCreationRole
        entity.invitationExpiryDays = domain.companySettings.invitationExpiryDays
        entity.maxTeamsPerProject = domain.companySettings.maxTeamsPerProject
        entity.maxUsersPerTeam = domain.companySettings.maxUsersPerTeam
    }
}

object CompanyMemberMapper {
    fun toDomain(entity: CompanyMemberEntity): CompanyMember = CompanyMember(
        id = entity.id.value,
        companyId = entity.companyId,
        userId = entity.userId,
        role = entity.role,
        joinedAt = entity.joinedAt.toEpochMilli(),
        invitedBy = entity.invitedBy,
        isActive = entity.isActive
    )

    fun toEntity(domain: CompanyMember): CompanyMemberEntity =
        CompanyMemberEntity.findById(domain.id) ?: CompanyMemberEntity.new(domain.id) {
            applyDomainToEntity(this, domain)
        }

    fun updateEntity(entity: CompanyMemberEntity, domain: CompanyMember) {
        applyDomainToEntity(entity, domain)
    }

    private fun applyDomainToEntity(entity: CompanyMemberEntity, domain: CompanyMember) {
        entity.companyId = domain.companyId
        entity.userId = domain.userId
        entity.role = domain.role
        entity.joinedAt = Instant.ofEpochMilli(domain.joinedAt)
        entity.invitedBy = domain.invitedBy
        entity.invitedAt = Instant.ofEpochMilli(domain.invitedAt)
        entity.isActive = domain.isActive
    }
}

object CompanyInvitationMapper {
    fun toDomain(entity: CompanyInvitationEntity): CompanyInvitation = CompanyInvitation(
        id = entity.id.value,
        companyId = entity.companyId,
        invitedBy = entity.invitedBy,
        identifier = entity.identifier,
        role = entity.role,
        status = entity.status,
        token = entity.token,
        expiresAt = entity.expiresAt.toEpochMilli(),
        createdAt = entity.createdAt.toEpochMilli(),
        acceptedAt = entity.acceptedAt?.toEpochMilli(),
        message = entity.message
    )

    fun toEntity(domain: CompanyInvitation): CompanyInvitationEntity =
        CompanyInvitationEntity.findById(domain.id) ?: CompanyInvitationEntity.new(domain.id) {
            applyDomainToEntity(this, domain)
        }

    fun updateEntity(entity: CompanyInvitationEntity, domain: CompanyInvitation) {
        applyDomainToEntity(entity, domain)
    }

    private fun applyDomainToEntity(entity: CompanyInvitationEntity, domain: CompanyInvitation) {
        entity.companyId = domain.companyId
        entity.invitedBy = domain.invitedBy
        entity.identifier = domain.identifier
        entity.role = domain.role
        entity.status = domain.status
        entity.token = domain.token
        entity.expiresAt = Instant.ofEpochMilli(domain.expiresAt)
        entity.createdAt = Instant.ofEpochMilli(domain.createdAt)
        entity.acceptedAt = domain.acceptedAt?.let { Instant.ofEpochMilli(it) }
        entity.message = domain.message
    }
}