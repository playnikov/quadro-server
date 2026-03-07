package com.quadro.datasource.repositories.task

import com.quadro.domain.models.task.Task
import com.quadro.domain.models.task.TaskListFilters
import com.quadro.domain.models.task.TaskResolution
import com.quadro.domain.models.task.TaskStatus
import java.util.UUID

interface TaskRepository {
    suspend fun create(task: Task): Task
    suspend fun findById(id: UUID): Task?
    suspend fun findByKey(projectId: UUID, key: String): Task?
    suspend fun findByProject(projectId: UUID, filters: TaskListFilters): List<Task>
    suspend fun findByAssignee(userId: UUID, projectId: UUID?): List<Task>
    suspend fun findByReporter(userId: UUID, projectId: UUID?): List<Task>
    suspend fun findByParent(parentId: UUID): List<Task>
    suspend fun update(task: Task): Task
    suspend fun delete(id: UUID): Boolean

    suspend fun updateStatus(id: UUID, status: TaskStatus, resolution: TaskResolution?): Boolean
    suspend fun updateAssignee(id: UUID, assigneeId: UUID?): Boolean
    suspend fun updateOrder(id: UUID, order: Int): Boolean

    suspend fun countByProject(projectId: UUID, filters: TaskListFilters): Long
    suspend fun countByAssignee(userId: UUID, status: TaskStatus?): Long

    suspend fun search(projectId: UUID, query: String, limit: Int): List<Task>
    suspend fun getNextOrder(projectId: UUID): Int
    suspend fun generateNextKey(projectId: UUID): String
}