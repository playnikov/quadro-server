package com.quadro.team.infrastructure.database.mappers

import com.quadro.shared.utils.toKotlinInstant
import com.quadro.shared.utils.toOffsetDateTime
import com.quadro.team.domain.models.TeamMember
import com.quadro.team.domain.models.TeamRole
import com.quadro.team.infrastructure.database.entities.TeamMembersEntity


object TeamMembersMapper {
    fun toDomain(entity: TeamMembersEntity): TeamMember = TeamMember(
        id = entity.id.value,
        teamId = entity.teamId,
        userId = entity.userId,
        role = TeamRole.valueOf(entity.role),
        joinedAt = entity.joinedAt?.toKotlinInstant(),
        invitedAt = entity.invitedAt?.toKotlinInstant(),
        invitedBy = entity.invitedBy,
        isActive = entity.isActive
    )

    fun toEntity(domain: TeamMember): TeamMembersEntity = TeamMembersEntity.findById(domain.id) ?: TeamMembersEntity.new(domain.id) {
        applyDomainToEntity(this, domain)
    }

    fun updateEntity(entity: TeamMembersEntity, domain: TeamMember) {
        applyDomainToEntity(entity, domain)
    }

    private fun applyDomainToEntity(entity: TeamMembersEntity, domain: TeamMember) {
        entity.teamId = domain.teamId
        entity.userId = domain.userId
        entity.role = domain.role.name
        entity.joinedAt = domain.joinedAt?.toOffsetDateTime()
        entity.invitedBy = domain.invitedBy
        entity.invitedAt = domain.invitedAt?.toOffsetDateTime()
        entity.isActive = domain.isActive
    }
}