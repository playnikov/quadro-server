package com.quadro.task.infrastructure.database.mappers.task

import com.quadro.shared.utils.toKotlinInstant
import com.quadro.shared.utils.toOffsetDateTime
import com.quadro.task.domain.models.task.TaskComment
import com.quadro.task.infrastructure.database.entities.task.TaskCommentEntity

object TaskCommentMapper {
    fun toDomain(entity: TaskCommentEntity): TaskComment = TaskComment(
        id = entity.id.value,
        taskId = entity.taskId,
        authorId = entity.authorId,
        content = entity.content,
        createdAt = entity.createdAt.toKotlinInstant(),
        updatedAt = entity.updatedAt.toKotlinInstant(),
        isEdited = entity.isEdited,
        parentId = entity.parentId,
        isDeleted = entity.isDeleted,
        mentions = entity.mentions ?: emptyList()
    )

    fun toEntity(domain: TaskComment): TaskCommentEntity =
        TaskCommentEntity.findById(domain.id) ?: TaskCommentEntity.new(domain.id) {
            applyDomainToEntity(this, domain)
        }

    fun updateEntity(entity: TaskCommentEntity, domain: TaskComment) {
        applyDomainToEntity(entity, domain)
    }

    private fun applyDomainToEntity(entity: TaskCommentEntity, domain: TaskComment) {
        entity.taskId = domain.taskId
        entity.authorId = domain.authorId
        entity.content = domain.content
        entity.parentId = domain.parentId
        entity.isDeleted = domain.isDeleted
        entity.isEdited = domain.isEdited
        entity.createdAt = domain.createdAt.toOffsetDateTime()
        entity.updatedAt = domain.updatedAt.toOffsetDateTime()
        entity.mentions = domain.mentions
    }
}