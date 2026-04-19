package com.quadro.company.infrastructure.database.mappers

import com.quadro.company.domain.models.CompanyInvitation
import com.quadro.company.domain.models.CompanyRole
import com.quadro.company.domain.models.InvitationStatus
import com.quadro.company.domain.models.InvitationType
import com.quadro.company.infrastructure.database.entities.CompanyInvitationEntity
import com.quadro.shared.utils.toKotlinInstant
import com.quadro.shared.utils.toOffsetDateTime

object CompanyInvitationMapper {
    fun toDomain(entity: CompanyInvitationEntity): CompanyInvitation = CompanyInvitation(
        id = entity.id.value,
        companyId = entity.companyId,
        invitedBy = entity.invitedBy,
        inviteType = InvitationType.valueOf(entity.inviteType),
        identifier = entity.identifier,
        role = CompanyRole.valueOf(entity.role),
        status = InvitationStatus.valueOf(entity.status),
        token = entity.token,
        expiresAt = entity.expiresAt.toKotlinInstant(),
        createdAt = entity.createdAt.toKotlinInstant(),
        acceptedAt = entity.acceptedAt?.toKotlinInstant(),
        acceptedBy = entity.acceptedBy,
        message = entity.message
    )

    fun toEntity(domain: CompanyInvitation): CompanyInvitationEntity =
        CompanyInvitationEntity.findById(domain.id) ?: CompanyInvitationEntity.new(domain.id) {
            applyDomainToEntity(this, domain)
        }

    private fun applyDomainToEntity(entity: CompanyInvitationEntity, domain: CompanyInvitation) {
        entity.companyId = domain.companyId
        entity.invitedBy = domain.invitedBy
        entity.inviteType = domain.inviteType.name
        entity.identifier = domain.identifier
        entity.role = domain.role.name
        entity.status = domain.status.name
        entity.token = domain.token
        entity.expiresAt = domain.expiresAt.toOffsetDateTime()
        entity.createdAt = domain.createdAt.toOffsetDateTime()
        entity.acceptedAt = domain.acceptedAt?.toOffsetDateTime()
        entity.acceptedBy = domain.acceptedBy
        entity.message = domain.message
    }
}