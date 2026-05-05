package com.quadro.task.infrastructure.database.mappers.team

import com.quadro.shared.utils.toKotlinInstant
import com.quadro.shared.utils.toOffsetDateTime
import com.quadro.task.domain.models.team.Team
import com.quadro.task.domain.models.team.TeamStatus
import com.quadro.task.infrastructure.database.entities.team.TeamEntity

object TeamMapper {
    fun toDomain(entity: TeamEntity): Team = Team(
        id = entity.id.value,
        status = TeamStatus.valueOf(entity.status)
    )

    fun toEntity(domain: Team): TeamEntity = TeamEntity.findById(domain.id) ?: TeamEntity.Companion.new(domain.id) {
        applyDomainToEntity(this, domain)
    }

    fun updateEntity(entity: TeamEntity, domain: Team) {
        applyDomainToEntity(entity, domain)
    }

    private fun applyDomainToEntity(entity: TeamEntity, domain: Team) {
        entity.status = domain.status.name
    }
}