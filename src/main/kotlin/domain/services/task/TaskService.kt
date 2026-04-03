package com.quadro.domain.services.task

import com.quadro.domain.models.task.Task
import com.quadro.domain.models.task.TaskAttachment
import com.quadro.domain.models.task.TaskComment
import com.quadro.domain.models.task.TaskCommentCreate
import com.quadro.domain.models.task.TaskCreate
import com.quadro.domain.models.task.TaskHistory
import com.quadro.domain.models.task.TaskListFilters
import com.quadro.domain.models.task.TaskPermissions
import com.quadro.domain.models.task.TaskResolution
import com.quadro.domain.models.task.TaskStatus
import com.quadro.domain.models.task.TaskTimeLog
import com.quadro.domain.models.task.TaskTimeLogCreate
import com.quadro.domain.models.task.TaskTimeStats
import com.quadro.domain.models.task.TaskUpdate
import com.quadro.domain.models.task.TaskWatcher
import java.util.UUID

interface TaskService {
    // Task CRUD
    suspend fun createTask(userId: UUID, request: TaskCreate): Result<Task>
    suspend fun getTask(taskId: UUID, userId: UUID): Result<Task>
    suspend fun getTaskByKey(projectId: UUID, key: String, userId: UUID): Result<Task>
    suspend fun updateTask(taskId: UUID, userId: UUID, request: TaskUpdate): Result<Task>
    suspend fun deleteTask(taskId: UUID, userId: UUID): Result<Unit>

    // Task listing
    suspend fun getProjectTasks(projectId: UUID, userId: UUID, filters: TaskListFilters): Result<List<Task>>
    suspend fun getUserTasks(userId: UUID, projectId: UUID?): Result<List<Task>>
    suspend fun getSubtasks(parentId: UUID, userId: UUID): Result<List<Task>>
    suspend fun searchTasks(projectId: UUID, userId: UUID, query: String): Result<List<Task>>

    // Task operations
    suspend fun changeStatus(taskId: UUID, userId: UUID, status: TaskStatus, resolution: TaskResolution?): Result<Task>
    suspend fun assignTask(taskId: UUID, userId: UUID, assigneeId: UUID?): Result<Task>
    suspend fun addSubtask(parentId: UUID, userId: UUID, request: TaskCreate): Result<Task>
    suspend fun moveTask(taskId: UUID, userId: UUID, newOrder: Int): Result<Unit>
    suspend fun bulkUpdateStatus(projectId: UUID, userId: UUID, taskIds: List<UUID>, status: TaskStatus): Result<Int>

    // Comments
    suspend fun addComment(taskId: UUID, userId: UUID, request: TaskCommentCreate): Result<TaskComment>
    suspend fun getComments(taskId: UUID, userId: UUID, page: Int, size: Int): Result<List<TaskComment>>
    suspend fun updateComment(commentId: UUID, userId: UUID, content: String): Result<TaskComment>
    suspend fun deleteComment(commentId: UUID, userId: UUID): Result<Unit>

    // Attachments
    suspend fun addAttachment(taskId: UUID, userId: UUID, fileName: String, fileSize: Long, mimeType: String, url: String): Result<TaskAttachment>
    suspend fun getAttachments(taskId: UUID, userId: UUID): Result<List<TaskAttachment>>
    suspend fun deleteAttachment(attachmentId: UUID, userId: UUID): Result<Unit>

    // Watchers
    suspend fun addWatcher(taskId: UUID, userId: UUID): Result<Unit>
    suspend fun removeWatcher(taskId: UUID, userId: UUID): Result<Unit>
    suspend fun getWatchers(taskId: UUID, userId: UUID): Result<List<TaskWatcher>>
    suspend fun isWatching(taskId: UUID, userId: UUID): Result<Boolean>

    // Time tracking
    suspend fun logTime(taskId: UUID, userId: UUID, request: TaskTimeLogCreate): Result<TaskTimeLog>
    suspend fun getTimeLogs(taskId: UUID, userId: UUID): Result<List<TaskTimeLog>>
    suspend fun getTimeStats(taskId: UUID, userId: UUID): Result<TaskTimeStats>
    suspend fun deleteTimeLog(timeLogId: UUID, userId: UUID): Result<Unit>

    // History
    suspend fun getTaskHistory(taskId: UUID, userId: UUID, page: Int, size: Int): Result<List<TaskHistory>>

    // Stats
    suspend fun getProjectTaskStats(projectId: UUID, userId: UUID): Result<Map<String, Any>>
    suspend fun getUserTaskStats(userId: UUID, projectId: UUID?): Result<Map<String, Any>>

    // Permissions
    suspend fun getTaskPermissions(taskId: UUID, userId: UUID): Result<TaskPermissions>
}