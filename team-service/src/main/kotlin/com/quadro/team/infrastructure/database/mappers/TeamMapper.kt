package com.quadro.team.infrastructure.database.mappers

import com.quadro.shared.utils.toKotlinInstant
import com.quadro.shared.utils.toOffsetDateTime
import com.quadro.team.domain.models.Team
import com.quadro.team.domain.models.TeamStatus
import com.quadro.team.domain.models.TeamVisibility
import com.quadro.team.infrastructure.database.entities.TeamEntity

object TeamMapper {
    fun toDomain(entity: TeamEntity): Team = Team(
        id = entity.id.value,
        companyId = entity.companyId,
        name = entity.name,
        description = entity.description,
        avatar = entity.avatar,
        status = TeamStatus.valueOf(entity.status),
        visibility = TeamVisibility.valueOf(entity.visibility),
        leadId = entity.leadId,
        createdBy = entity.createdBy,
        createdAt = entity.createdAt.toKotlinInstant(),
        updatedAt = entity.updatedAt.toKotlinInstant(),
    )

    fun toEntity(domain: Team): TeamEntity = TeamEntity.findById(domain.id) ?: TeamEntity.new(domain.id) {
        applyDomainToEntity(this, domain)
    }

    fun updateEntity(entity: TeamEntity, domain: Team) {
        applyDomainToEntity(entity, domain)
    }

    private fun applyDomainToEntity(entity: TeamEntity, domain: Team) {
        entity.companyId = domain.companyId
        entity.name = domain.name
        entity.description = domain.description
        entity.avatar = domain.avatar
        entity.status = domain.status.name
        entity.visibility = domain.visibility.name
        entity.leadId = domain.leadId
        entity.createdBy = domain.createdBy
        entity.createdAt = domain.createdAt.toOffsetDateTime()
        entity.updatedAt = domain.updatedAt.toOffsetDateTime()
    }
}