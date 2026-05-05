package com.quadro.task.infrastructure.database.mappers.task

import com.quadro.task.domain.models.task.Task
import com.quadro.task.domain.models.task.TaskPriority
import com.quadro.task.domain.models.task.TaskStatus
import com.quadro.task.domain.models.task.TaskType
import com.quadro.task.infrastructure.database.entities.task.TaskEntity
import com.quadro.shared.utils.toKotlinInstant
import com.quadro.shared.utils.toOffsetDateTime

object TaskMapper {
    fun toDomain(entity: TaskEntity): Task = Task(
        id = entity.id.value,
        projectId = entity.projectId,
        sprintId = entity.sprintId,
        parentTaskId = entity.parentTaskId,
        number = entity.number,
        title = entity.title,
        description = entity.description,
        status = TaskStatus.valueOf(entity.status),
        priority = TaskPriority.valueOf(entity.priority),
        type = TaskType.valueOf(entity.type),
        assigneeId = entity.assigneeId,
        assignedTeamId = entity.assignedTeamId,
        reporterId = entity.reporterId,
        storyPoints = entity.storyPoints,
        estimatedHours = entity.estimatedHours,
        loggedHours = entity.loggedHours,
        dueDate = entity.dueDate?.toKotlinInstant(),
        startedAt = entity.startedAt?.toKotlinInstant(),
        completedAt = entity.completedAt?.toKotlinInstant(),
        createdAt = entity.createdAt.toKotlinInstant(),
        updatedAt = entity.updatedAt.toKotlinInstant(),
        labels = entity.labels ?: emptyList()
    )

    fun toEntity(domain: Task): TaskEntity =
        TaskEntity.findById(domain.id) ?: TaskEntity.new(domain.id) {
            applyDomainToEntity(this, domain)
        }

    fun updateEntity(entity: TaskEntity, domain: Task) {
        applyDomainToEntity(entity, domain)
    }

    private fun applyDomainToEntity(entity: TaskEntity, domain: Task) {
        entity.projectId = domain.projectId
        entity.sprintId = domain.sprintId
        entity.parentTaskId = domain.parentTaskId
        entity.number = domain.number
        entity.title = domain.title
        entity.description = domain.description
        entity.status = domain.status.name
        entity.priority = domain.priority.name
        entity.type = domain.type.name
        entity.assigneeId = domain.assigneeId
        entity.assignedTeamId = domain.assignedTeamId
        entity.reporterId = domain.reporterId
        entity.storyPoints = domain.storyPoints
        entity.estimatedHours = domain.estimatedHours
        entity.loggedHours = domain.loggedHours
        entity.dueDate = domain.dueDate?.toOffsetDateTime()
        entity.startedAt = domain.startedAt?.toOffsetDateTime()
        entity.completedAt = domain.completedAt?.toOffsetDateTime()
        entity.createdAt = domain.createdAt.toOffsetDateTime()
        entity.updatedAt = domain.updatedAt.toOffsetDateTime()
        entity.labels = domain.labels
    }
}