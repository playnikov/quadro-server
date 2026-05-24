package com.quadro.task.infrastructure.database.mappers.project

import com.quadro.shared.utils.toKotlinInstant
import com.quadro.task.domain.models.project.Project
import com.quadro.task.domain.models.project.ProjectStatus
import com.quadro.task.infrastructure.database.entities.project.ProjectEntity

object ProjectMapper {
    fun toDomain(entity: ProjectEntity) = Project(
        id = entity.id.value,
        key = entity.key,
        status = entity.status
    )

    fun newEntity(domain: Project): ProjectEntity =
        ProjectEntity.findById(domain.id) ?: ProjectEntity.Companion.new(domain.id) {
            applyDomainToEntity(this, domain)
        }

    fun updateEntity(entity: ProjectEntity, domain: Project) {
        applyDomainToEntity(entity, domain)
    }

    private fun applyDomainToEntity(entity: ProjectEntity, domain: Project) {
        entity.key = domain.key
        entity.status = domain.status
    }
}