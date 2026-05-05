package com.quadro.task.infrastructure.database.mappers.task

import com.quadro.shared.utils.toKotlinInstant
import com.quadro.shared.utils.toOffsetDateTime
import com.quadro.task.domain.models.task.HistoryAction
import com.quadro.task.domain.models.task.TaskHistory
import com.quadro.task.infrastructure.database.entities.task.TaskHistoryEntity

object TaskHistoryMapper {
    fun toDomain(entity: TaskHistoryEntity): TaskHistory = TaskHistory(
        id = entity.id.value,
        taskId = entity.taskId,
        userId = entity.userId,
        action = HistoryAction.valueOf(entity.action),
        oldValue = entity.oldValue,
        newValue = entity.newValue,
        createdAt = entity.createdAt.toKotlinInstant()
    )

    fun toEntity(domain: TaskHistory): TaskHistoryEntity =
        TaskHistoryEntity.findById(domain.id) ?: TaskHistoryEntity.new(domain.id) {
            applyDomainToEntity(this, domain)
        }

    fun updateEntity(entity: TaskHistoryEntity, domain: TaskHistory) {
        applyDomainToEntity(entity, domain)
    }

    private fun applyDomainToEntity(entity: TaskHistoryEntity, domain: TaskHistory) {
        entity.taskId = domain.taskId
        entity.userId = domain.userId
        entity.action = domain.action.name
        entity.oldValue = domain.oldValue
        entity.newValue = domain.newValue
        entity.createdAt = domain.createdAt.toOffsetDateTime()
    }
}