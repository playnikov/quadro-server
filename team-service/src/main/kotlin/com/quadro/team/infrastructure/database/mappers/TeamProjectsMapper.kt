package com.quadro.team.infrastructure.database.mappers

import com.quadro.shared.utils.toKotlinInstant
import com.quadro.shared.utils.toOffsetDateTime
import com.quadro.team.domain.models.TeamProjectBinding
import com.quadro.team.domain.models.TeamProjectRole
import com.quadro.team.infrastructure.database.entities.TeamProjectsEntity

object TeamProjectsMapper {
    fun toDomain(entity: TeamProjectsEntity): TeamProjectBinding = TeamProjectBinding(
        id = entity.id.value,
        teamId = entity.teamId,
        projectId = entity.projectId,
        role = TeamProjectRole.valueOf(entity.role),
        boundAt = entity.boundAt.toKotlinInstant(),
        boundBy = entity.boundBy
    )

    fun toEntity(domain: TeamProjectBinding): TeamProjectsEntity = TeamProjectsEntity.findById(domain.id) ?: TeamProjectsEntity.new(domain.id) {
        applyDomainToEntity(this, domain)
    }

    fun updateEntity(entity: TeamProjectsEntity, domain: TeamProjectBinding) {
        applyDomainToEntity(entity, domain)
    }

    private fun applyDomainToEntity(entity: TeamProjectsEntity, domain: TeamProjectBinding) {
        entity.teamId = domain.teamId
        entity.projectId = domain.projectId
        entity.role = domain.role.name
        entity.boundAt = domain.boundAt.toOffsetDateTime()
        entity.boundBy = domain.boundBy
    }
}