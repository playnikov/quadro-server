package com.quadro.domain.services.task

import com.quadro.datasource.repositories.project.ProjectMemberRepository
import com.quadro.datasource.repositories.project.ProjectRepository
import com.quadro.datasource.repositories.task.TaskAttachmentRepository
import com.quadro.datasource.repositories.task.TaskCommentRepository
import com.quadro.datasource.repositories.task.TaskHistoryRepository
import com.quadro.datasource.repositories.task.TaskRepository
import com.quadro.datasource.repositories.task.TaskTimeLogRepository
import com.quadro.datasource.repositories.task.TaskWatcherRepository
import com.quadro.datasource.repositories.users.UserRepository
import com.quadro.domain.models.project.ProjectRole
import com.quadro.domain.models.task.HistoryField
import com.quadro.domain.models.task.NotificationLevel
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
import com.quadro.domain.models.task.TaskType
import com.quadro.domain.models.task.TaskUpdate
import com.quadro.domain.models.task.TaskWatcher
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import java.util.UUID

class TaskServiceImpl(
    private val taskRepository: TaskRepository,
    private val taskCommentRepository: TaskCommentRepository,
    private val taskAttachmentRepository: TaskAttachmentRepository,
    private val taskWatcherRepository: TaskWatcherRepository,
    private val taskTimeLogRepository: TaskTimeLogRepository,
    private val taskHistoryRepository: TaskHistoryRepository,
    private val projectRepository: ProjectRepository,
    private val projectMemberRepository: ProjectMemberRepository,
    private val userRepository: UserRepository
) : TaskService {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val json = Json { ignoreUnknownKeys = true }

    // ============== Task CRUD ==============

    override suspend fun createTask(
        userId: UUID,
        request: TaskCreate
    ): Result<Task> {
        return try {
            val project = projectRepository.findById(request.projectId)
                ?: return Result.failure(Exception("Project not found"))

            val member = projectMemberRepository.findByProjectAndUser(request.projectId, userId)
            if (!canCreateTask(member?.role)) {
                return Result.failure(Exception("Insufficient permissions to create task"))
            }

            request.parentId?.let { parentId ->
                val parent = taskRepository.findById(parentId)
                    ?: return Result.failure(Exception("Parent task not found"))
                if (parent.projectId != request.projectId) {
                    return Result.failure(Exception("Parent task belongs to different project"))
                }
                if (parent.type == TaskType.SUBTASK) {
                    return Result.failure(Exception("Cannot add subtask to another subtask"))
                }
            }

            request.assigneeId?.let { assigneeId ->
                if (!projectMemberRepository.exists(request.projectId, assigneeId)) {
                    return Result.failure(Exception("Assignee is not a member of this project"))
                }
            }

            val now = System.currentTimeMillis()
            val nextOrder = taskRepository.getNextOrder(request.projectId)
            val key = taskRepository.generateNextKey(request.projectId)

            val task = Task(
                id = UUID.randomUUID(),
                projectId = request.projectId,
                parentId = request.parentId,
                key = key,
                title = request.title,
                description = request.description,
                type = request.type,
                status = TaskStatus.BACKLOG,
                priority = request.priority,
                resolution = null,
                assigneeId = request.assigneeId,
                reporterId = userId,
                storyPoints = request.storyPoints,
                timeEstimate = request.timeEstimate,
                timeSpent = 0,
                dueDate = request.dueDate,
                startedAt = null,
                completedAt = null,
                createdAt = now,
                updatedAt = now,
                order = nextOrder,
                tags = request.tags?.let { json.encodeToString(it) }
            )

            val createdTask = taskRepository.create(task)

            taskWatcherRepository.addWatcher(createdTask.id, userId, NotificationLevel.ALL)

            logger.info("Task created: ${createdTask.key} in project: ${request.projectId}")

            Result.success(createdTask)
        } catch (e: Exception) {
            logger.error("Failed to create task", e)
            Result.failure(e)
        }
    }

    override suspend fun getTask(
        taskId: UUID,
        userId: UUID
    ): Result<Task> {
        return try {
            val task = taskRepository.findById(taskId)
                ?: return Result.failure(Exception("Task not found"))

            if (!projectMemberRepository.exists(task.projectId, userId)) {
                return Result.failure(Exception("Access denied"))
            }

            Result.success(task)
        } catch (e: Exception) {
            logger.error("Failed to get task", e)
            Result.failure(e)
        }
    }

    override suspend fun getTaskByKey(
        projectId: UUID,
        key: String,
        userId: UUID
    ): Result<Task> {
        return try {
            val task = taskRepository.findByKey(projectId, key)
                ?: return Result.failure(Exception("Task not found"))

            if (!projectMemberRepository.exists(projectId, userId)) {
                return Result.failure(Exception("Access denied"))
            }

            Result.success(task)

        } catch (e: Exception) {
            logger.error("Failed to get task by key", e)
            Result.failure(e)
        }
    }

    override suspend fun updateTask(
        taskId: UUID,
        userId: UUID,
        request: TaskUpdate
    ): Result<Task> {
        return try {
            val task = taskRepository.findById(taskId)
                ?: return Result.failure(Exception("Task not found"))

            val member = projectMemberRepository.findByProjectAndUser(task.projectId, userId)
            if (!canEditTask(member?.role, task.assigneeId == userId)) {
                return Result.failure(Exception("Insufficient permissions to edit task"))
            }

            val now = System.currentTimeMillis()

            val updatedTask = task.copy(
                title = request.title ?: task.title,
                description = request.description ?: task.description,
                type = request.type ?: task.type,
                priority = request.priority ?: task.priority,
                assigneeId = request.assigneeId ?: task.assigneeId,
                storyPoints = request.storyPoints ?: task.storyPoints,
                timeEstimate = request.timeEstimate ?: task.timeEstimate,
                timeSpent = request.timeSpent ?: task.timeSpent,
                dueDate = request.dueDate ?: task.dueDate,
                tags = request.tags?.let { json.encodeToString(it) } ?: task.tags,
                updatedAt = now
            )

            val savedTask = taskRepository.update(updatedTask)

            logger.info("Task updated: ${savedTask.key}")

            Result.success(savedTask)
        } catch (e: Exception) {
            logger.error("Failed to update task", e)
            Result.failure(e)
        }
    }

    override suspend fun deleteTask(taskId: UUID, userId: UUID): Result<Unit> {
        return try {
            val task = taskRepository.findById(taskId)
                ?: return Result.failure(Exception("Task not found"))

            val member = projectMemberRepository.findByProjectAndUser(task.projectId, userId)
            if (!canDeleteTask(member?.role)) {
                return Result.failure(Exception("Insufficient permissions to delete task"))
            }

            val subtasks = taskRepository.findByParent(taskId)
            if (subtasks.isNotEmpty()) {
                return Result.failure(Exception("Cannot delete task with subtasks"))
            }

            taskCommentRepository.deleteByTask(taskId)
            taskAttachmentRepository.deleteByTask(taskId)
            taskWatcherRepository.removeAllByTask(taskId)
            taskTimeLogRepository.deleteByTask(taskId)
            taskHistoryRepository.deleteByTask(taskId)

            taskRepository.delete(taskId)

            logger.info("Task deleted: ${task.key} by user: $userId")
            Result.success(Unit)
        } catch (e: Exception) {
            logger.error("Failed to delete task", e)
            Result.failure(e)
        }
    }

    // ============== Task Listing ==============

    override suspend fun getProjectTasks(
        projectId: UUID,
        userId: UUID,
        filters: TaskListFilters
    ): Result<List<Task>> {
        return try {
            if (!projectMemberRepository.exists(projectId, userId)) {
                return Result.failure(Exception("Access denied"))
            }

            val tasks = taskRepository.findByProject(projectId, filters)
            Result.success(tasks)

        } catch (e: Exception) {
            logger.error("Failed to get project tasks", e)
            Result.failure(e)
        }
    }

    override suspend fun getUserTasks(
        userId: UUID,
        projectId: UUID?
    ): Result<List<Task>> {
        return try {
            val tasks = taskRepository.findByAssignee(userId, projectId)
            Result.success(tasks)
        } catch (e: Exception) {
            logger.error("Failed to get user tasks", e)
            Result.failure(e)
        }
    }

    override suspend fun getSubtasks(
        parentId: UUID,
        userId: UUID
    ): Result<List<Task>> {
        return try {
            val parent = taskRepository.findById(parentId)
                ?: return Result.failure(Exception("Parent task not found"))

            if (!projectMemberRepository.exists(parent.projectId, userId)) {
                return Result.failure(Exception("Access denied"))
            }

            val subtasks = taskRepository.findByParent(parentId)
            Result.success(subtasks)

        } catch (e: Exception) {
            logger.error("Failed to get subtasks", e)
            Result.failure(e)
        }
    }

    override suspend fun searchTasks(
        projectId: UUID,
        userId: UUID,
        query: String
    ): Result<List<Task>> {
        return try {
            if (!projectMemberRepository.exists(projectId, userId)) {
                return Result.failure(Exception("Access denied"))
            }

            val tasks = taskRepository.search(projectId, query, 10)
            Result.success(tasks)

        } catch (e: Exception) {
            logger.error("Failed to search tasks", e)
            Result.failure(e)
        }
    }

    // ============== Task Operations ==============

    override suspend fun changeStatus(
        taskId: UUID,
        userId: UUID,
        status: TaskStatus,
        resolution: TaskResolution?
    ): Result<Task> {
        return try {
            val task = taskRepository.findById(taskId)
                ?: return Result.failure(Exception("Task not found"))

            val member = projectMemberRepository.findByProjectAndUser(task.projectId, userId)
            if (!canChangeStatus(member?.role, task.assigneeId == userId)) {
                return Result.failure(Exception("Insufficient permissions to change status"))
            }

            val oldStatus = task.status
            val now = System.currentTimeMillis()

            val updatedTask = task.copy(
                status = status,
                resolution = if (status == TaskStatus.DONE || status == TaskStatus.CANCELLED) resolution else null,
                startedAt = if (status == TaskStatus.IN_PROGRESS && task.startedAt == null) now else task.startedAt,
                completedAt = if (status == TaskStatus.DONE) now else null,
                updatedAt = now
            )

            taskRepository.update(updatedTask)

            if (status == TaskStatus.DONE && task.parentId != null) {
                checkParentCompletion(task.parentId)
            }

            logger.info("Task status changed: ${task.key} from $oldStatus to $status")

            Result.success(updatedTask)
        } catch (e: Exception) {
            logger.error("Failed to change task status", e)
            Result.failure(e)
        }
    }

    override suspend fun assignTask(
        taskId: UUID,
        userId: UUID,
        assigneeId: UUID?
    ): Result<Task> {
        return try {
            val task = taskRepository.findById(taskId)
                ?: return Result.failure(Exception("Task not found"))

            val member = projectMemberRepository.findByProjectAndUser(task.projectId, userId)
            if (!canAssign(member?.role)) {
                return Result.failure(Exception("Insufficient permissions to assign task"))
            }

            assigneeId?.let { id ->
                if (!projectMemberRepository.exists(task.projectId, id)) {
                    return Result.failure(Exception("Assignee is not a member of this project"))
                }
            }

            val oldAssignee = task.assigneeId
            val now = System.currentTimeMillis()

            val updatedTask = task.copy(
                assigneeId = assigneeId,
                updatedAt = now
            )

            taskRepository.update(updatedTask)

            val newName = assigneeId?.let { userRepository.findById(it)?.username } ?: "Unassigned"

            assigneeId?.let { id ->
                if (!taskWatcherRepository.isWatching(taskId, id)) {
                    taskWatcherRepository.addWatcher(taskId, id, NotificationLevel.ALL)
                }
            }

            logger.info("Task assigned: ${task.key} to $newName")
            Result.success(updatedTask)
        } catch (e: Exception) {
            logger.error("Failed to assign task", e)
            Result.failure(e)
        }
    }

    override suspend fun addSubtask(
        parentId: UUID,
        userId: UUID,
        request: TaskCreate
    ): Result<Task> {
        TODO("Not yet implemented")
    }

    override suspend fun moveTask(
        taskId: UUID,
        userId: UUID,
        newOrder: Int
    ): Result<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun bulkUpdateStatus(
        projectId: UUID,
        userId: UUID,
        taskIds: List<UUID>,
        status: TaskStatus
    ): Result<Int> {
        TODO("Not yet implemented")
    }

    override suspend fun addComment(
        taskId: UUID,
        userId: UUID,
        request: TaskCommentCreate
    ): Result<TaskComment> {
        TODO("Not yet implemented")
    }

    override suspend fun getComments(
        taskId: UUID,
        userId: UUID,
        page: Int,
        size: Int
    ): Result<List<TaskComment>> {
        TODO("Not yet implemented")
    }

    override suspend fun updateComment(
        commentId: UUID,
        userId: UUID,
        content: String
    ): Result<TaskComment> {
        TODO("Not yet implemented")
    }

    override suspend fun deleteComment(commentId: UUID, userId: UUID): Result<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun addAttachment(
        taskId: UUID,
        userId: UUID,
        fileName: String,
        fileSize: Long,
        mimeType: String,
        url: String
    ): Result<TaskAttachment> {
        TODO("Not yet implemented")
    }

    override suspend fun getAttachments(
        taskId: UUID,
        userId: UUID
    ): Result<List<TaskAttachment>> {
        TODO("Not yet implemented")
    }

    override suspend fun deleteAttachment(
        attachmentId: UUID,
        userId: UUID
    ): Result<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun addWatcher(taskId: UUID, userId: UUID): Result<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun removeWatcher(taskId: UUID, userId: UUID): Result<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun getWatchers(
        taskId: UUID,
        userId: UUID
    ): Result<List<TaskWatcher>> {
        TODO("Not yet implemented")
    }

    override suspend fun isWatching(taskId: UUID, userId: UUID): Result<Boolean> {
        TODO("Not yet implemented")
    }

    override suspend fun logTime(
        taskId: UUID,
        userId: UUID,
        request: TaskTimeLogCreate
    ): Result<TaskTimeLog> {
        TODO("Not yet implemented")
    }

    override suspend fun getTimeLogs(
        taskId: UUID,
        userId: UUID
    ): Result<List<TaskTimeLog>> {
        TODO("Not yet implemented")
    }

    override suspend fun getTimeStats(
        taskId: UUID,
        userId: UUID
    ): Result<TaskTimeStats> {
        TODO("Not yet implemented")
    }

    override suspend fun deleteTimeLog(timeLogId: UUID, userId: UUID): Result<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun getTaskHistory(
        taskId: UUID,
        userId: UUID,
        page: Int,
        size: Int
    ): Result<List<TaskHistory>> {
        return try {
            val task = taskRepository.findById(taskId)
                ?: return Result.failure(Exception("Task not found"))

            if (!projectMemberRepository.exists(task.projectId, userId)) {
                return Result.failure(Exception("Access denied"))
            }

            val offset = (page - 1) * size
            val history = taskHistoryRepository.findByTask(taskId, size, offset)
            Result.success(history)

        } catch (e: Exception) {
            logger.error("Failed to get task history", e)
            Result.failure(e)
        }
    }

    override suspend fun getProjectTaskStats(
        projectId: UUID,
        userId: UUID
    ): Result<Map<String, Any>> {
        TODO("Not yet implemented")
    }

    override suspend fun getUserTaskStats(
        userId: UUID,
        projectId: UUID?
    ): Result<Map<String, Any>> {
        TODO("Not yet implemented")
    }

    override suspend fun getTaskPermissions(
        taskId: UUID,
        userId: UUID
    ): Result<TaskPermissions> {
        return try {
            val task = taskRepository.findById(taskId)
                ?: return Result.failure(Exception("Task not found"))

            if (!projectMemberRepository.exists(task.projectId, userId)) {
                return Result.failure(Exception("Access denied"))
            }

            val member = projectMemberRepository.findByProjectAndUser(task.projectId, userId)
            Result.success(TaskPermissions.fromRole(member?.role, task.assigneeId == userId))

        } catch (e: Exception) {
            logger.error("Failed to get task permissions", e)
            Result.failure(e)
        }
    }

    // ============== Private Methods ==============

    private fun canCreateTask(role: ProjectRole?): Boolean {
        return role in listOf(ProjectRole.OWNER, ProjectRole.LEAD, ProjectRole.ADMIN, ProjectRole.MEMBER)
    }

    private fun canEditTask(role: ProjectRole?, isAssignee: Boolean): Boolean {
        return when {
            role in listOf(ProjectRole.OWNER, ProjectRole.LEAD, ProjectRole.ADMIN) -> true
            isAssignee && role == ProjectRole.MEMBER -> true
            else -> false
        }
    }

    private fun canDeleteTask(role: ProjectRole?): Boolean {
        return role in listOf(ProjectRole.OWNER, ProjectRole.LEAD, ProjectRole.ADMIN)
    }

    private fun canChangeStatus(role: ProjectRole?, isAssignee: Boolean): Boolean {
        return when {
            role in listOf(ProjectRole.OWNER, ProjectRole.LEAD, ProjectRole.ADMIN) -> true
            isAssignee -> true
            else -> false
        }
    }

    private fun canAssign(role: ProjectRole?): Boolean {
        return role in listOf(ProjectRole.OWNER, ProjectRole.LEAD, ProjectRole.ADMIN)
    }

    private fun canBulkUpdate(role: ProjectRole?): Boolean {
        return role in listOf(ProjectRole.OWNER, ProjectRole.LEAD, ProjectRole.ADMIN)
    }

    private fun canDeleteAnyComment(role: ProjectRole?): Boolean {
        return role in listOf(ProjectRole.OWNER, ProjectRole.LEAD, ProjectRole.ADMIN)
    }

    private fun canDeleteAnyAttachment(role: ProjectRole?): Boolean {
        return role in listOf(ProjectRole.OWNER, ProjectRole.LEAD, ProjectRole.ADMIN)
    }

    private suspend fun checkParentCompletion(parentId: UUID) {
        val subtasks = taskRepository.findByParent(parentId)
        if (subtasks.all { it.status == TaskStatus.DONE }) {
            val parent = taskRepository.findById(parentId)!!
            changeStatus(parentId, parent.reporterId, TaskStatus.DONE, null)
        }
    }

    private suspend fun logHistory(
        taskId: UUID,
        userId: UUID,
        field: HistoryField,
        oldValue: String?,
        newValue: String?
    ) {
        val history = TaskHistory(
            id = UUID.randomUUID(),
            taskId = taskId,
            userId = userId,
            field = field,
            oldValue = oldValue,
            newValue = newValue,
            createdAt = System.currentTimeMillis()
        )
        taskHistoryRepository.log(history)
    }

    private fun formatMinutes(minutes: Long?): String {
        if (minutes == null) return "0"
        val hours = minutes / 60
        val mins = minutes % 60
        return when {
            hours > 0 -> "${hours}h ${mins}m"
            else -> "${mins}m"
        }
    }
}