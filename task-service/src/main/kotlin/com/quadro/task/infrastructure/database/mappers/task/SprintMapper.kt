package com.quadro.task.infrastructure.database.mappers.task

import com.quadro.shared.utils.toKotlinInstant
import com.quadro.shared.utils.toOffsetDateTime
import com.quadro.task.domain.models.task.Sprint
import com.quadro.task.domain.models.task.SprintStatus
import com.quadro.task.infrastructure.database.entities.task.SprintEntity

object SprintMapper  {
    fun toDomain(entity: SprintEntity): Sprint = Sprint(
        id = entity.id.value,
        projectId = entity.projectId,
        name = entity.name,
        goal = entity.goal,
        status = entity.status,
        startDate = entity.startDate.toKotlinInstant(),
        endDate = entity.endDate.toKotlinInstant(),
        createdBy = entity.createdBy,
        createdAt = entity.createdAt.toKotlinInstant(),
        updatedAt = entity.updatedAt.toKotlinInstant()
    )

    fun toEntity(domain: Sprint): SprintEntity =
        SprintEntity.findById(domain.id) ?: SprintEntity.new(domain.id) {
            applyDomainToEntity(this, domain)
        }

    fun updateEntity(entity: SprintEntity, domain: Sprint) {
        applyDomainToEntity(entity, domain)
    }

    private fun applyDomainToEntity(entity: SprintEntity, domain: Sprint) {
        entity.projectId = domain.projectId
        entity.name = domain.name
        entity.goal = domain.goal
        entity.status = domain.status
        entity.startDate = domain.startDate.toOffsetDateTime()
        entity.endDate = domain.endDate.toOffsetDateTime()
        entity.createdBy = domain.createdBy
        entity.createdAt = domain.createdAt.toOffsetDateTime()
        entity.updatedAt = domain.updatedAt.toOffsetDateTime()
    }
}