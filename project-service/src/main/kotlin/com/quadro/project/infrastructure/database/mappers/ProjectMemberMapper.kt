package com.quadro.project.infrastructure.database.mappers

import com.quadro.project.domain.models.ProjectMember
import com.quadro.project.domain.models.ProjectRole
import com.quadro.project.infrastructure.database.entities.ProjectMemberEntity
import com.quadro.shared.utils.toKotlinInstant
import com.quadro.shared.utils.toOffsetDateTime

object ProjectMemberMapper {
    fun toDomain(entity: ProjectMemberEntity): ProjectMember = ProjectMember(
        id = entity.id.value,
        projectId = entity.projectId,
        userId = entity.userId,
        role = ProjectRole.valueOf(entity.role),
        joinedAt = entity.joinedAt.toKotlinInstant(),
        invitedBy = entity.invitedBy,
        invitedAt = entity.invitedAt.toKotlinInstant()
    )

    fun toEntity(domain: ProjectMember): ProjectMemberEntity =
        ProjectMemberEntity.findById(domain.id) ?: ProjectMemberEntity.new(domain.id) {
            applyDomainToEntity(this, domain)
        }

    fun updateEntity(entity: ProjectMemberEntity, domain: ProjectMember) {
        applyDomainToEntity(entity, domain)
    }

    private fun applyDomainToEntity(entity: ProjectMemberEntity, domain: ProjectMember) {
        entity.projectId = domain.projectId
        entity.userId = domain.userId
        entity.role = domain.role.name
        entity.joinedAt = domain.joinedAt.toOffsetDateTime()
        entity.invitedBy = domain.invitedBy
        entity.invitedAt = domain.invitedAt.toOffsetDateTime()
    }
}