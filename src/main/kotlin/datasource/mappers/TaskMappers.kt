package com.quadro.datasource.mappers

import com.quadro.datasource.entities.TaskAttachmentEntity
import com.quadro.datasource.entities.TaskCommentEntity
import com.quadro.datasource.entities.TaskEntity
import com.quadro.datasource.entities.TaskHistoryEntity
import com.quadro.datasource.entities.TaskTimeLogEntity
import com.quadro.datasource.entities.TaskWatcherEntity
import com.quadro.domain.models.task.HistoryField
import com.quadro.domain.models.task.Task
import com.quadro.domain.models.task.TaskAttachment
import com.quadro.domain.models.task.TaskComment
import com.quadro.domain.models.task.TaskHistory
import com.quadro.domain.models.task.TaskPriority
import com.quadro.domain.models.task.TaskResolution
import com.quadro.domain.models.task.TaskStatus
import com.quadro.domain.models.task.TaskTimeLog
import com.quadro.domain.models.task.TaskType
import com.quadro.domain.models.task.TaskWatcher
import java.time.Instant

object TaskMapper {
    fun toDomain(entity: TaskEntity): Task = Task(
        id = entity.id.value,
        projectId = entity.projectId,
        parentId = entity.parentId,
        key = entity.key,
        title = entity.title,
        description = entity.description,
        type = TaskType.valueOf(entity.type),
        status = TaskStatus.valueOf(entity.status),
        priority = TaskPriority.valueOf(entity.priority),
        resolution = entity.resolution?.let { TaskResolution.valueOf(it) },
        assigneeId = entity.assigneeId,
        reporterId = entity.reporterId,
        storyPoints = entity.storyPoints,
        timeEstimate = entity.timeEstimate,
        timeSpent = entity.timeSpent,
        dueDate = entity.dueDate?.toEpochMilli(),
        startedAt = entity.startedAt?.toEpochMilli(),
        completedAt = entity.completedAt?.toEpochMilli(),
        createdAt = entity.createdAt.toEpochMilli(),
        updatedAt = entity.updatedAt.toEpochMilli(),
        order = entity.order,
        tags = entity.tags
    )

    fun toEntity(domain: Task): TaskEntity = TaskEntity.findById(domain.id) ?: TaskEntity.new(domain.id) {
        applyDomainToEntity(this, domain)
    }

    fun updateEntity(entity: TaskEntity, domain: Task) {
        applyDomainToEntity(entity, domain)
    }

    private fun applyDomainToEntity(entity: TaskEntity, domain: Task) {
        entity.projectId = domain.projectId
        entity.parentId = domain.parentId
        entity.key = domain.key
        entity.title = domain.title
        entity.description = domain.description
        entity.type = domain.type.name
        entity.status = domain.status.name
        entity.priority = domain.priority.name
        entity.resolution = domain.resolution?.name
        entity.assigneeId = domain.assigneeId
        entity.reporterId = domain.reporterId
        entity.storyPoints = domain.storyPoints
        entity.timeEstimate = domain.timeEstimate
        entity.timeSpent = domain.timeSpent
        entity.dueDate = domain.dueDate?.let { Instant.ofEpochMilli(it) }
        entity.startedAt = domain.startedAt?.let { Instant.ofEpochMilli(it) }
        entity.completedAt = domain.completedAt?.let { Instant.ofEpochMilli(it) }
        entity.createdAt = Instant.ofEpochMilli(domain.createdAt)
        entity.updatedAt = Instant.ofEpochMilli(domain.updatedAt)
        entity.order = domain.order
        entity.tags = domain.tags
    }
}

object TaskCommentMapper {
    fun toDomain(entity: TaskCommentEntity): TaskComment = TaskComment(
        id = entity.id.value,
        taskId = entity.taskId,
        authorId = entity.authorId,
        content = entity.content,
        createdAt = entity.createdAt.toEpochMilli(),
        updatedAt = entity.updatedAt?.toEpochMilli(),
    )

    fun toEntity(domain: TaskComment): TaskCommentEntity = TaskCommentEntity.new(domain.id) {
        taskId = domain.taskId
        authorId = domain.authorId
        content = domain.content
        createdAt = Instant.ofEpochMilli(domain.createdAt)
        updatedAt = domain.updatedAt?.let { Instant.ofEpochMilli(it) }
    }
}

object TaskAttachmentMapper {

    fun toDomain(entity: TaskAttachmentEntity): TaskAttachment = TaskAttachment(
        id = entity.id.value,
        taskId = entity.taskId,
        uploadedBy = entity.uploadedBy,
        fileName = entity.fileName,
        fileSize = entity.fileSize,
        mimeType = entity.mimeType,
        url = entity.url,
        createdAt = entity.createdAt.toEpochMilli(),
    )

    fun toEntity(domain: TaskAttachment): TaskAttachmentEntity = TaskAttachmentEntity.new(domain.id) {
        taskId = domain.taskId
        uploadedBy = domain.uploadedBy
        fileName = domain.fileName
        fileSize = domain.fileSize
        mimeType = domain.mimeType
        url = domain.url
        createdAt = Instant.ofEpochMilli(domain.createdAt)
    }
}

object TaskWatcherMapper {

    fun toDomain(entity: TaskWatcherEntity): TaskWatcher = TaskWatcher(
        id = entity.id.value,
        taskId = entity.taskId,
        userId = entity.userId,
        addedAt = entity.addedAt.toEpochMilli(),
        notificationLevel = entity.notificationLevel
    )

    fun toEntity(domain: TaskWatcher): TaskWatcherEntity = TaskWatcherEntity.new(domain.id) {
        taskId = domain.taskId
        userId = domain.userId
        addedAt = Instant.ofEpochMilli(domain.addedAt)
        notificationLevel = domain.notificationLevel
    }
}

object TaskTimeLogMapper {

    fun toDomain(entity: TaskTimeLogEntity): TaskTimeLog = TaskTimeLog(
        id = entity.id.value,
        taskId = entity.taskId,
        userId = entity.userId,
        timeSpent = entity.timeSpent,
        description = entity.description,
        loggedAt = entity.loggedAt.toEpochMilli(),
    )

    fun toEntity(domain: TaskTimeLog): TaskTimeLogEntity = TaskTimeLogEntity.new(domain.id) {
        taskId = domain.taskId
        userId = domain.userId
        timeSpent = domain.timeSpent
        description = domain.description
        loggedAt = Instant.ofEpochMilli(domain.loggedAt)
    }
}

object TaskHistoryMapper {

    fun toDomain(entity: TaskHistoryEntity): TaskHistory = TaskHistory(
        id = entity.id.value,
        taskId = entity.taskId,
        userId = entity.userId,
        field = HistoryField.valueOf(entity.field),
        oldValue = entity.oldValue,
        newValue = entity.newValue,
        createdAt = entity.createdAt.toEpochMilli()
    )

    fun toEntity(domain: TaskHistory): TaskHistoryEntity = TaskHistoryEntity.new(domain.id) {
        taskId = domain.taskId
        userId = domain.userId
        field = domain.field.name
        oldValue = domain.oldValue
        newValue = domain.newValue
        createdAt = Instant.ofEpochMilli(domain.createdAt)
    }
}