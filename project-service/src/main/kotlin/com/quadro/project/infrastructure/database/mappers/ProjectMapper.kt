package com.quadro.project.infrastructure.database.mappers

import com.quadro.project.domain.models.Project
import com.quadro.project.domain.models.ProjectPriority
import com.quadro.project.domain.models.ProjectStatus
import com.quadro.project.domain.models.ProjectType
import com.quadro.project.domain.models.ProjectVisibility
import com.quadro.project.infrastructure.database.entities.ProjectEntity
import com.quadro.shared.utils.toKotlinInstant
import com.quadro.shared.utils.toOffsetDateTime
import kotlin.time.toJavaInstant

object ProjectMapper {
    fun toDomain(entity: ProjectEntity) = Project(
        id = entity.id.value,
        type = ProjectType.valueOf(entity.type),
        name = entity.name,
        key = entity.key,
        description = entity.description,
        status = ProjectStatus.valueOf(entity.status),
        priority = ProjectPriority.valueOf(entity.priority),
        visibility = ProjectVisibility.valueOf(entity.visibility),
        createdAt = entity.createdAt.toKotlinInstant(),
        updatedAt = entity.updatedAt.toKotlinInstant(),
    )

    fun toEntity(domain: Project): ProjectEntity =
        ProjectEntity.findById(domain.id) ?: ProjectEntity.new(domain.id) {
            applyDomainToEntity(this, domain)
        }

    fun updateEntity(entity: ProjectEntity, domain: Project) {
        applyDomainToEntity(entity, domain)
    }

    private fun applyDomainToEntity(entity: ProjectEntity, domain: Project) {
        entity.name = domain.name
        entity.type = domain.type.name
        entity.key = domain.key
        entity.description = domain.description
        entity.status = domain.status.name
        entity.priority = domain.priority.name
        entity.visibility = domain.visibility.name
        entity.createdAt = domain.createdAt.toOffsetDateTime()
        entity.updatedAt = domain.updatedAt.toOffsetDateTime()
    }
}