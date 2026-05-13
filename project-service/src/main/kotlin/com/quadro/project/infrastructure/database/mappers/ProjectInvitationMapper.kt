package com.quadro.project.infrastructure.database.mappers

import com.quadro.project.domain.models.InvitationStatus
import com.quadro.project.domain.models.InviteType
import com.quadro.project.domain.models.ProjectInvitation
import com.quadro.project.domain.models.ProjectRole
import com.quadro.project.infrastructure.database.entities.ProjectInvitationEntity
import com.quadro.shared.utils.toKotlinInstant
import com.quadro.shared.utils.toOffsetDateTime

object ProjectInvitationMapper {
    fun toDomain(entity: ProjectInvitationEntity): ProjectInvitation = ProjectInvitation(
        id = entity.id.value,
        projectId = entity.projectId,
        invitedBy = entity.invitedBy,
        inviteType = InviteType.valueOf(entity.inviteType),
        identifier = entity.identifier,
        role = ProjectRole.valueOf(entity.role),
        status = InvitationStatus.valueOf(entity.status),
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