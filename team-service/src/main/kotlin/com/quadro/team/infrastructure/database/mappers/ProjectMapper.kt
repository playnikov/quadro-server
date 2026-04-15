package com.quadro.team.infrastructure.database.mappers

import com.quadro.shared.utils.toKotlinInstant
import com.quadro.shared.utils.toOffsetDateTime
import com.quadro.team.domain.models.Project
import com.quadro.team.domain.models.ProjectStatus
import com.quadro.team.domain.models.Team
import com.quadro.team.domain.models.TeamStatus
import com.quadro.team.domain.models.TeamVisibility
import com.quadro.team.infrastructure.database.entities.ProjectEntity
import com.quadro.team.infrastructure.database.entities.TeamEntity

object ProjectMapper {
    fun toDomain(entity: ProjectEntity): Project = Project(
        id = entity.id.value,
        companyId = entity.companyId,
        name = entity.name,
        status = ProjectStatus.valueOf(entity.status),
        updatedAt = entity.updatedAt.toKotlinInstant(),
    )

    fun newEntity(domain: Project): ProjectEntity = ProjectEntity.findById(domain.id) ?: ProjectEntity.new(domain.id) {
        applyDomainToEntity(this, domain)
    }

    fun updateEntity(entity: ProjectEntity, domain: Project) {
        applyDomainToEntity(entity, domain)
    }

    private fun applyDomainToEntity(entity: ProjectEntity, domain: Project) {
        entity.companyId = domain.companyId
        entity.name = domain.name
        entity.status = domain.status.name
        entity.updatedAt = domain.updatedAt.toOffsetDateTime()
    }
}