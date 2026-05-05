package com.quadro.task.infrastructure.database.mappers.task

import com.quadro.shared.utils.toKotlinInstant
import com.quadro.shared.utils.toOffsetDateTime
import com.quadro.task.domain.models.task.TaskAttachment
import com.quadro.task.infrastructure.database.entities.task.TaskAttachmentEntity

object TaskAttachmentMapper {
    fun toDomain(entity: TaskAttachmentEntity): TaskAttachment = TaskAttachment(
        id = entity.id.value,
        taskId = entity.taskId,
        uploadedBy = entity.uploadedBy,
        fileName = entity.fileName,
        fileSize = entity.fileSize,
        url = entity.fileUrl,
        createdAt = entity.createdAt.toKotlinInstant(),
    )

    fun toEntity(domain: TaskAttachment): TaskAttachmentEntity =
        TaskAttachmentEntity.findById(domain.id) ?: TaskAttachmentEntity.new(domain.id) {
            applyDomainToEntity(this, domain)
        }

    fun updateEntity(entity: TaskAttachmentEntity, domain: TaskAttachment) {
        applyDomainToEntity(entity, domain)
    }

    private fun applyDomainToEntity(entity: TaskAttachmentEntity, domain: TaskAttachment) {
        entity.taskId = domain.taskId
        entity.uploadedBy = domain.uploadedBy
        entity.fileName = domain.fileName
        entity.fileSize = domain.fileSize
        entity.fileUrl = domain.url
        entity.createdAt = domain.createdAt.toOffsetDateTime()
    }
}