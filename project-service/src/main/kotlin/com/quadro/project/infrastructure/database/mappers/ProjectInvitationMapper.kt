package com.quadro.project.infrastructure.database.mappers

import com.quadro.project.domain.models.InviteStatus
import com.quadro.project.domain.models.InviteType
import com.quadro.project.domain.models.ProjectInvitation
import com.quadro.project.domain.models.MemberRole
import com.quadro.project.infrastructure.database.entities.ProjectInvitationEntity
import com.quadro.shared.utils.toKotlinInstant
import com.quadro.shared.utils.toOffsetDateTime

object ProjectInvitationMapper {
    fun toDomain(entity: ProjectInvitationEntity): ProjectInvitation = ProjectInvitation(
        id = entity.id.value,
        projectId = entity.projectId,
        invitedBy = entity.invitedBy,
        type = entity.type,
        identifier = entity.identifier,
        role = entity.role,
        status = entity.status,
        token = entity.token,
        expiresAt = entity.expiresAt.toKotlinInstant(),
        createdAt = entity.createdAt.toKotlinInstant(),
        acceptedAt = entity.acceptedAt?.toKotlinInstant(),
        acceptedBy = entity.acceptedBy,
        message = entity.message
    )

    fun toEntity(domain: ProjectInvitation): ProjectInvitationEntity =
        ProjectInvitationEntity.findById(domain.id) ?: ProjectInvitationEntity.new(domain.id) {
            applyDomainToEntity(this, domain)
        }

    private fun applyDomainToEntity(entity: ProjectInvitationEntity, domain: ProjectInvitation) {
        entity.projectId = domain.projectId
        entity.invitedBy = domain.invitedBy
        entity.type = domain.type
        entity.identifier = domain.identifier
        entity.role = domain.role
        entity.status = domain.status
        entity.token = domain.token
        entity.expiresAt = domain.expiresAt.toOffsetDateTime()
        entity.createdAt = domain.createdAt.toOffsetDateTime()
        entity.acceptedAt = domain.acceptedAt?.toOffsetDateTime()
        entity.acceptedBy = domain.acceptedBy
        entity.message = domain.message
    }
}