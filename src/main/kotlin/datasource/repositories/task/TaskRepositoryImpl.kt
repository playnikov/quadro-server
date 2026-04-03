package com.quadro.datasource.repositories.task

import com.quadro.datasource.entities.ProjectEntity
import com.quadro.datasource.entities.TaskEntity
import com.quadro.datasource.entities.TasksTable
import com.quadro.datasource.mappers.TaskMapper
import com.quadro.domain.models.task.Task
import com.quadro.domain.models.task.TaskListFilters
import com.quadro.domain.models.task.TaskResolution
import com.quadro.domain.models.task.TaskStatus
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.lowerCase
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.time.Instant
import java.util.UUID

class TaskRepositoryImpl : TaskRepository {
    override suspend fun create(task: Task): Task = newSuspendedTransaction {
        TaskMapper.toDomain(TaskMapper.toEntity(task))
    }

    override suspend fun findById(id: UUID): Task? = newSuspendedTransaction {
        TaskEntity.findById(id)?.let { TaskMapper.toDomain(it) }
    }

    override suspend fun findByKey(projectId: UUID, key: String): Task? = newSuspendedTransaction {
        TaskEntity.find {
            (TasksTable.projectId eq projectId) and
                    (TasksTable.key eq key)
        }.firstOrNull()?.let { TaskMapper.toDomain(it) }
    }

    override suspend fun findByProject(
        projectId: UUID,
        filters: TaskListFilters
    ): List<Task> {
        TODO("Not yet implemented")
    }

    override suspend fun findByAssignee(
        userId: UUID,
        projectId: UUID?
    ): List<Task> = newSuspendedTransaction {
        val query = TaskEntity.find { TasksTable.assigneeId eq userId }
        val filtered = projectId?.let { query.filter { it.projectId == projectId } } ?: query
        filtered.sortedByDescending { it.createdAt }.map { TaskMapper.toDomain(it) }
    }

    override suspend fun findByReporter(
        userId: UUID,
        projectId: UUID?
    ): List<Task> = newSuspendedTransaction {
        val query = TaskEntity.find { TasksTable.reporterId eq userId }
        val filtered = projectId?.let { query.filter { it.projectId == projectId } } ?: query
        filtered.sortedByDescending { it.createdAt }.map { TaskMapper.toDomain(it) }
    }

    override suspend fun findByParent(parentId: UUID): List<Task> = newSuspendedTransaction {
        TaskEntity.find { TasksTable.parentId eq parentId }
            .sortedBy { it.order }
            .map { TaskMapper.toDomain(it) }
    }

    override suspend fun update(task: Task): Task = newSuspendedTransaction {
        val entity = TaskEntity.findById(task.id)
            ?: throw IllegalArgumentException("Task not found with id: ${task.id}")

        TaskMapper.updateEntity(entity, task)
        TaskMapper.toDomain(entity)
    }

    override suspend fun delete(id: UUID): Boolean = newSuspendedTransaction {
        TaskEntity.findById(id)?.delete() != null
    }

    override suspend fun updateStatus(
        id: UUID,
        status: TaskStatus,
        resolution: TaskResolution?
    ): Boolean = newSuspendedTransaction {
        TaskEntity.findById(id)?.apply {
            this.status = status.name
            this.resolution = resolution?.name
            if (status == TaskStatus.IN_PROGRESS && startedAt == null) {
                startedAt = Instant.now()
            }
            if (status == TaskStatus.DONE) {
                completedAt = Instant.now()
            }
            updatedAt = Instant.now()
        } != null
    }

    override suspend fun updateAssignee(id: UUID, assigneeId: UUID?): Boolean = newSuspendedTransaction {
        TaskEntity.findById(id)?.apply {
            this.assigneeId = assigneeId
            updatedAt = Instant.now()
        } != null
    }

    override suspend fun updateOrder(id: UUID, order: Int): Boolean = newSuspendedTransaction {
        TaskEntity.findById(id)?.apply {
            this.order = order
            updatedAt = Instant.now()
        } != null
    }

    override suspend fun countByProject(
        projectId: UUID,
        filters: TaskListFilters
    ): Long {
        TODO("Not yet implemented")
    }

    override suspend fun countByAssignee(
        userId: UUID,
        status: TaskStatus?
    ): Long {
        TODO("Not yet implemented")
    }

    override suspend fun search(
        projectId: UUID,
        query: String,
        limit: Int
    ): List<Task> = newSuspendedTransaction {
        val searchPattern = "%${query.lowercase()}%"
        TaskEntity.find {
            (TasksTable.projectId eq projectId) and
                    (TasksTable.title.lowerCase() like searchPattern or
                            (TasksTable.key.lowerCase() like searchPattern) or
                            (TasksTable.description.lowerCase() like searchPattern))
        }
            .limit(limit)
            .sortedByDescending { it.createdAt }
            .map { TaskMapper.toDomain(it) }
    }

    override suspend fun getNextOrder(projectId: UUID): Int = newSuspendedTransaction {
        val maxOrder = TaskEntity.find { TasksTable.projectId eq projectId }
            .maxByOrNull { it.order }?.order ?: 0
        maxOrder + 1
    }

    override suspend fun generateNextKey(projectId: UUID): String = newSuspendedTransaction {
        val project = ProjectEntity.findById(projectId)
            ?: throw IllegalArgumentException("Project not found")

        val prefix = project.key.uppercase()
        val count = TaskEntity.find { TasksTable.projectId eq projectId }.count()

        "${prefix}-${count + 1}"
    }
}