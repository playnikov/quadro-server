package com.quadro

import com.quadro.datasource.repositories.project.ProjectMemberRepository
import com.quadro.datasource.repositories.project.ProjectRepository
import com.quadro.datasource.repositories.task.TaskAttachmentRepository
import com.quadro.datasource.repositories.task.TaskCommentRepository
import com.quadro.datasource.repositories.task.TaskHistoryRepository
import com.quadro.datasource.repositories.task.TaskRepository
import com.quadro.datasource.repositories.task.TaskTimeLogRepository
import com.quadro.datasource.repositories.task.TaskWatcherRepository
import com.quadro.datasource.repositories.users.UserRepository
import com.quadro.domain.models.project.Project
import com.quadro.domain.models.project.ProjectMember
import com.quadro.domain.models.project.ProjectRole
import com.quadro.domain.models.project.ProjectStatus
import com.quadro.domain.models.project.ProjectType
import com.quadro.domain.models.project.ProjectVisibility
import com.quadro.domain.models.task.HistoryField
import com.quadro.domain.models.task.NotificationLevel
import com.quadro.domain.models.task.Task
import com.quadro.domain.models.task.TaskCreate
import com.quadro.domain.models.task.TaskHistory
import com.quadro.domain.models.task.TaskListFilters
import com.quadro.domain.models.task.TaskPriority
import com.quadro.domain.models.task.TaskResolution
import com.quadro.domain.models.task.TaskStatus
import com.quadro.domain.models.task.TaskType
import com.quadro.domain.models.task.TaskUpdate
import com.quadro.domain.models.user.DomainUserRole
import com.quadro.domain.models.user.User
import com.quadro.domain.services.task.TaskServiceImpl
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.runBlocking
import org.junit.Before
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class TaskServiceTest {
    private lateinit var taskRepository: TaskRepository
    private lateinit var taskCommentRepository: TaskCommentRepository
    private lateinit var taskAttachmentRepository: TaskAttachmentRepository
    private lateinit var taskWatcherRepository: TaskWatcherRepository
    private lateinit var taskTimeLogRepository: TaskTimeLogRepository
    private lateinit var taskHistoryRepository: TaskHistoryRepository
    private lateinit var projectRepository: ProjectRepository
    private lateinit var projectMemberRepository: ProjectMemberRepository
    private lateinit var userRepository: UserRepository
    private lateinit var taskService: TaskServiceImpl

    private val testProjectId = UUID.randomUUID()
    private val testUserId = UUID.randomUUID()
    private val testTaskId = UUID.randomUUID()
    private val testParentTaskId = UUID.randomUUID()
    private val testAssigneeId = UUID.randomUUID()
    private val testKey = "TEST-1"

    @Before
    fun setup() {
        taskRepository = mockk(relaxed = true)
        taskCommentRepository = mockk(relaxed = true)
        taskAttachmentRepository = mockk(relaxed = true)
        taskWatcherRepository = mockk(relaxed = true)
        taskTimeLogRepository = mockk(relaxed = true)
        taskHistoryRepository = mockk(relaxed = true)
        projectRepository = mockk(relaxed = true)
        projectMemberRepository = mockk(relaxed = true)
        userRepository = mockk(relaxed = true)

        taskService = TaskServiceImpl(
            taskRepository = taskRepository,
            taskCommentRepository = taskCommentRepository,
            taskAttachmentRepository = taskAttachmentRepository,
            taskWatcherRepository = taskWatcherRepository,
            taskTimeLogRepository = taskTimeLogRepository,
            taskHistoryRepository = taskHistoryRepository,
            projectRepository = projectRepository,
            projectMemberRepository = projectMemberRepository,
            userRepository = userRepository
        )
    }

    // ============== Тесты createTask ==============

    @Test
    fun `createTask - should create task successfully`() = runBlocking {
        // Arrange
        val project = createTestProject()
        val member = createTestProjectMember(role = ProjectRole.MEMBER)
        val request = TaskCreate(
            projectId = testProjectId,
            title = "Test Task",
            description = "Test Description",
            type = TaskType.TASK,
            priority = TaskPriority.HIGH,
            assigneeId = testAssigneeId,
            storyPoints = 5,
            timeEstimate = 480,
            dueDate = System.currentTimeMillis() + 86400000,
            tags = listOf("backend", "api")
        )

        coEvery { projectRepository.findById(testProjectId) } returns project
        coEvery { projectMemberRepository.findByProjectAndUser(testProjectId, testUserId) } returns member
        coEvery { projectMemberRepository.exists(testProjectId, testAssigneeId) } returns true
        coEvery { taskRepository.getNextOrder(testProjectId) } returns 1
        coEvery { taskRepository.generateNextKey(testProjectId) } returns testKey
        coEvery { taskRepository.create(any()) } answers { firstArg() }
        coEvery { taskWatcherRepository.addWatcher(any(), testUserId, NotificationLevel.ALL) } returns mockk()

        // Act
        val result = taskService.createTask(testUserId, request)

        // Assert
        assertTrue(result.isSuccess)
        val task = result.getOrNull()
        assertEquals("Test Task", task?.title)
        assertEquals(testKey, task?.key)
        assertEquals(TaskStatus.BACKLOG, task?.status)
        assertEquals(testUserId, task?.reporterId)

        coVerify(exactly = 1) { taskRepository.create(any()) }
        coVerify(exactly = 1) { taskWatcherRepository.addWatcher(any(), testUserId, NotificationLevel.ALL) }
    }

    @Test
    fun `createTask - should fail when project not found`() = runBlocking {
        // Arrange
        val request = TaskCreate(
            projectId = testProjectId,
            title = "Test Task"
        )

        coEvery { projectRepository.findById(testProjectId) } returns null

        // Act
        val result = taskService.createTask(testUserId, request)

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Project not found", result.exceptionOrNull()?.message)
    }

    @Test
    fun `createTask - should fail with insufficient permissions`() = runBlocking {
        // Arrange
        val project = createTestProject()
        val member = createTestProjectMember(role = ProjectRole.VIEWER) // VIEWER не может создавать
        val request = TaskCreate(
            projectId = testProjectId,
            title = "Test Task"
        )

        coEvery { projectRepository.findById(testProjectId) } returns project
        coEvery { projectMemberRepository.findByProjectAndUser(testProjectId, testUserId) } returns member

        // Act
        val result = taskService.createTask(testUserId, request)

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Insufficient permissions to create task", result.exceptionOrNull()?.message)
    }

    @Test
    fun `createTask - should validate assignee is in project`() = runBlocking {
        // Arrange
        val project = createTestProject()
        val member = createTestProjectMember(role = ProjectRole.MEMBER)
        val request = TaskCreate(
            projectId = testProjectId,
            title = "Test Task",
            assigneeId = testAssigneeId
        )

        coEvery { projectRepository.findById(testProjectId) } returns project
        coEvery { projectMemberRepository.findByProjectAndUser(testProjectId, testUserId) } returns member
        coEvery { projectMemberRepository.exists(testProjectId, testAssigneeId) } returns false // Не в проекте

        // Act
        val result = taskService.createTask(testUserId, request)

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Assignee is not a member of this project", result.exceptionOrNull()?.message)
    }

    @Test
    fun `createTask - should validate parent task`() = runBlocking {
        // Arrange
        val project = createTestProject()
        val member = createTestProjectMember(role = ProjectRole.MEMBER)
        val parentTask = createTestTask(id = testParentTaskId, type = TaskType.SUBTASK) // SUBTASK не может быть родителем
        val request = TaskCreate(
            projectId = testProjectId,
            parentId = testParentTaskId,
            title = "Test Subtask"
        )

        coEvery { projectRepository.findById(testProjectId) } returns project
        coEvery { projectMemberRepository.findByProjectAndUser(testProjectId, testUserId) } returns member
        coEvery { taskRepository.findById(testParentTaskId) } returns parentTask

        // Act
        val result = taskService.createTask(testUserId, request)

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Cannot add subtask to another subtask", result.exceptionOrNull()?.message)
    }

    // ============== Тесты getTask ==============

    @Test
    fun `getTask - should return task when user has access`() = runBlocking {
        // Arrange
        val task = createTestTask()

        coEvery { taskRepository.findById(testTaskId) } returns task
        coEvery { projectMemberRepository.exists(testProjectId, testUserId) } returns true

        // Act
        val result = taskService.getTask(testTaskId, testUserId)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(testTaskId, result.getOrNull()?.id)
    }

    @Test
    fun `getTask - should fail when task not found`() = runBlocking {
        // Arrange
        coEvery { taskRepository.findById(testTaskId) } returns null

        // Act
        val result = taskService.getTask(testTaskId, testUserId)

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Task not found", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getTask - should fail when user has no access`() = runBlocking {
        // Arrange
        val task = createTestTask()

        coEvery { taskRepository.findById(testTaskId) } returns task
        coEvery { projectMemberRepository.exists(testProjectId, testUserId) } returns false

        // Act
        val result = taskService.getTask(testTaskId, testUserId)

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Access denied", result.exceptionOrNull()?.message)
    }

    // ============== Тесты getTaskByKey ==============

    @Test
    fun `getTaskByKey - should return task by key`() = runBlocking {
        // Arrange
        val task = createTestTask()

        coEvery { taskRepository.findByKey(testProjectId, testKey) } returns task
        coEvery { projectMemberRepository.exists(testProjectId, testUserId) } returns true

        // Act
        val result = taskService.getTaskByKey(testProjectId, testKey, testUserId)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(testTaskId, result.getOrNull()?.id)
    }

    @Test
    fun `getTaskByKey - should fail when key not found`() = runBlocking {
        // Arrange
        coEvery { taskRepository.findByKey(testProjectId, "NONE") } returns null

        // Act
        val result = taskService.getTaskByKey(testProjectId, "NONE", testUserId)

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Task not found", result.exceptionOrNull()?.message)
    }

    // ============== Тесты updateTask ==============

    @Test
    fun `updateTask - should update task when user is admin`() = runBlocking {
        // Arrange
        val task = createTestTask()
        val member = createTestProjectMember(role = ProjectRole.ADMIN)
        val request = TaskUpdate(
            title = "Updated Title",
            description = "Updated Description",
            priority = TaskPriority.HIGH
        )

        coEvery { taskRepository.findById(testTaskId) } returns task
        coEvery { projectMemberRepository.findByProjectAndUser(testProjectId, testUserId) } returns member
        coEvery { taskRepository.update(any()) } answers { firstArg() }

        // Act
        val result = taskService.updateTask(testTaskId, testUserId, request)

        // Assert
        assertTrue(result.isSuccess)
        val updatedTask = result.getOrNull()
        assertEquals("Updated Title", updatedTask?.title)
        assertEquals("Updated Description", updatedTask?.description)

        coVerify(exactly = 1) { taskRepository.update(any()) }
    }

    @Test
    fun `updateTask - should update task when user is assignee`() = runBlocking {
        // Arrange
        val task = createTestTask(assigneeId = testUserId) // Пользователь - исполнитель
        val member = createTestProjectMember(role = ProjectRole.MEMBER)
        val request = TaskUpdate(title = "Updated Title")

        coEvery { taskRepository.findById(testTaskId) } returns task
        coEvery { projectMemberRepository.findByProjectAndUser(testProjectId, testUserId) } returns member
        coEvery { taskRepository.update(any()) } answers { firstArg() }

        // Act
        val result = taskService.updateTask(testTaskId, testUserId, request)

        // Assert
        assertTrue(result.isSuccess)

        coVerify(exactly = 1) { taskRepository.update(any()) }
    }

    @Test
    fun `updateTask - should fail with insufficient permissions`() = runBlocking {
        // Arrange
        val task = createTestTask(assigneeId = UUID.randomUUID()) // Не исполнитель
        val member = createTestProjectMember(role = ProjectRole.MEMBER) // MEMBER без прав
        val request = TaskUpdate(title = "Updated Title")

        coEvery { taskRepository.findById(testTaskId) } returns task
        coEvery { projectMemberRepository.findByProjectAndUser(testProjectId, testUserId) } returns member

        // Act
        val result = taskService.updateTask(testTaskId, testUserId, request)

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Insufficient permissions to edit task", result.exceptionOrNull()?.message)
    }

    // ============== Тесты deleteTask ==============

    @Test
    fun `deleteTask - should delete task when user has permission`() = runBlocking {
        // Arrange
        val task = createTestTask()
        val member = createTestProjectMember(role = ProjectRole.ADMIN)

        coEvery { taskRepository.findById(testTaskId) } returns task
        coEvery { projectMemberRepository.findByProjectAndUser(testProjectId, testUserId) } returns member
        coEvery { taskRepository.findByParent(testTaskId) } returns emptyList()
        coEvery { taskRepository.delete(testTaskId) } returns true

        // Act
        val result = taskService.deleteTask(testTaskId, testUserId)

        // Assert
        assertTrue(result.isSuccess)

        coVerify(exactly = 1) { taskCommentRepository.deleteByTask(testTaskId) }
        coVerify(exactly = 1) { taskAttachmentRepository.deleteByTask(testTaskId) }
        coVerify(exactly = 1) { taskWatcherRepository.removeAllByTask(testTaskId) }
        coVerify(exactly = 1) { taskTimeLogRepository.deleteByTask(testTaskId) }
        coVerify(exactly = 1) { taskHistoryRepository.deleteByTask(testTaskId) }
        coVerify(exactly = 1) { taskRepository.delete(testTaskId) }
    }

    @Test
    fun `deleteTask - should fail when task has subtasks`() = runBlocking {
        // Arrange
        val task = createTestTask()
        val member = createTestProjectMember(role = ProjectRole.ADMIN)
        val subtask = createTestTask(id = UUID.randomUUID())

        coEvery { taskRepository.findById(testTaskId) } returns task
        coEvery { projectMemberRepository.findByProjectAndUser(testProjectId, testUserId) } returns member
        coEvery { taskRepository.findByParent(testTaskId) } returns listOf(subtask)

        // Act
        val result = taskService.deleteTask(testTaskId, testUserId)

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Cannot delete task with subtasks", result.exceptionOrNull()?.message)

        coVerify(exactly = 0) { taskRepository.delete(any()) }
    }

    // ============== Тесты getProjectTasks ==============

    @Test
    fun `getProjectTasks - should return project tasks`() = runBlocking {
        // Arrange
        val tasks = listOf(
            createTestTask(id = UUID.randomUUID()),
            createTestTask(id = UUID.randomUUID())
        )
        val filters = TaskListFilters(page = 1, size = 20)

        coEvery { projectMemberRepository.exists(testProjectId, testUserId) } returns true
        coEvery { taskRepository.findByProject(testProjectId, filters) } returns tasks

        // Act
        val result = taskService.getProjectTasks(testProjectId, testUserId, filters)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull()?.size)
    }

    @Test
    fun `getProjectTasks - should fail when user has no access`() = runBlocking {
        // Arrange
        val filters = TaskListFilters()

        coEvery { projectMemberRepository.exists(testProjectId, testUserId) } returns false

        // Act
        val result = taskService.getProjectTasks(testProjectId, testUserId, filters)

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Access denied", result.exceptionOrNull()?.message)
    }

    // ============== Тесты getUserTasks ==============

    @Test
    fun `getUserTasks - should return user tasks`() = runBlocking {
        // Arrange
        val tasks = listOf(
            createTestTask(id = UUID.randomUUID()),
            createTestTask(id = UUID.randomUUID())
        )

        coEvery { taskRepository.findByAssignee(testUserId, null) } returns tasks

        // Act
        val result = taskService.getUserTasks(testUserId, null)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull()?.size)
    }

    @Test
    fun `getUserTasks - should filter by project`() = runBlocking {
        // Arrange
        coEvery { taskRepository.findByAssignee(testUserId, testProjectId) } returns emptyList()

        // Act
        taskService.getUserTasks(testUserId, testProjectId)

        // Assert
        coVerify(exactly = 1) { taskRepository.findByAssignee(testUserId, testProjectId) }
    }

    // ============== Тесты getSubtasks ==============

    @Test
    fun `getSubtasks - should return subtasks`() = runBlocking {
        // Arrange
        val parent = createTestTask(id = testParentTaskId)
        val subtasks = listOf(
            createTestTask(id = UUID.randomUUID()),
            createTestTask(id = UUID.randomUUID())
        )

        coEvery { taskRepository.findById(testParentTaskId) } returns parent
        coEvery { projectMemberRepository.exists(testProjectId, testUserId) } returns true
        coEvery { taskRepository.findByParent(testParentTaskId) } returns subtasks

        // Act
        val result = taskService.getSubtasks(testParentTaskId, testUserId)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull()?.size)
    }

    @Test
    fun `getSubtasks - should fail when parent not found`() = runBlocking {
        // Arrange
        coEvery { taskRepository.findById(testParentTaskId) } returns null

        // Act
        val result = taskService.getSubtasks(testParentTaskId, testUserId)

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Parent task not found", result.exceptionOrNull()?.message)
    }

    // ============== Тесты searchTasks ==============

    @Test
    fun `searchTasks - should return search results`() = runBlocking {
        // Arrange
        val tasks = listOf(
            createTestTask(id = UUID.randomUUID(), title = "Backend API"),
            createTestTask(id = UUID.randomUUID(), title = "Backend DB")
        )

        coEvery { projectMemberRepository.exists(testProjectId, testUserId) } returns true
        coEvery { taskRepository.search(testProjectId, "backend", 10) } returns tasks

        // Act
        val result = taskService.searchTasks(testProjectId, testUserId, "backend")

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull()?.size)
    }

    @Test
    fun `searchTasks - should fail when user has no access`() = runBlocking {
        // Arrange
        coEvery { projectMemberRepository.exists(testProjectId, testUserId) } returns false

        // Act
        val result = taskService.searchTasks(testProjectId, testUserId, "test")

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Access denied", result.exceptionOrNull()?.message)
    }

    // ============== Тесты changeStatus ==============

    @Test
    fun `changeStatus - should change task status`() = runBlocking {
        // Arrange
        val task = createTestTask(status = TaskStatus.TODO, assigneeId = testUserId)
        val member = createTestProjectMember(role = ProjectRole.MEMBER)

        coEvery { taskRepository.findById(testTaskId) } returns task
        coEvery { projectMemberRepository.findByProjectAndUser(testProjectId, testUserId) } returns member
        coEvery { taskRepository.update(any()) } answers { firstArg() }

        // Act
        val result = taskService.changeStatus(testTaskId, testUserId, TaskStatus.IN_PROGRESS, null)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(TaskStatus.IN_PROGRESS, result.getOrNull()?.status)
        assertNotNull(result.getOrNull()?.startedAt)

        coVerify(exactly = 1) { taskRepository.update(any()) }
    }

    @Test
    fun `changeStatus - should complete parent when all subtasks done`() = runBlocking {
        // Arrange
        val parentTask = createTestTask(id = testParentTaskId, status = TaskStatus.IN_PROGRESS, assigneeId = testUserId)
        val task = createTestTask(
            id = testTaskId,
            parentId = testParentTaskId,
            status = TaskStatus.IN_PROGRESS,
            assigneeId = testUserId
        )
        val member = createTestProjectMember(role = ProjectRole.MEMBER)

        coEvery { taskRepository.findById(testTaskId) } returns task
        coEvery { taskRepository.findById(testParentTaskId) } returns parentTask
        coEvery { projectMemberRepository.findByProjectAndUser(testProjectId, testUserId) } returns member
        coEvery { taskRepository.update(any()) } answers { firstArg() }
        coEvery { taskRepository.findByParent(testParentTaskId) } returns emptyList()

        // Act
        val result = taskService.changeStatus(testTaskId, testUserId, TaskStatus.DONE, TaskResolution.FIXED)

        // Assert
        assertTrue(result.isSuccess)

        coVerify(exactly = 2) { taskRepository.update(any()) }
    }

    // ============== Тесты assignTask ==============

    @Test
    fun `assignTask - should assign task to user`() = runBlocking {
        // Arrange
        val task = createTestTask(assigneeId = null)
        val member = createTestProjectMember(role = ProjectRole.ADMIN)
        val assignee = createTestUser()

        coEvery { taskRepository.findById(testTaskId) } returns task
        coEvery { projectMemberRepository.findByProjectAndUser(testProjectId, testUserId) } returns member
        coEvery { projectMemberRepository.exists(testProjectId, testAssigneeId) } returns true
        coEvery { userRepository.findById(testAssigneeId) } returns assignee
        coEvery { taskWatcherRepository.isWatching(testTaskId, testAssigneeId) } returns false
        coEvery { taskWatcherRepository.addWatcher(testTaskId, testAssigneeId, NotificationLevel.ALL) } returns mockk()
        coEvery { taskRepository.update(any()) } answers { firstArg() }

        // Act
        val result = taskService.assignTask(testTaskId, testUserId, testAssigneeId)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(testAssigneeId, result.getOrNull()?.assigneeId)

        coVerify(exactly = 1) { taskRepository.update(any()) }
        coVerify(exactly = 1) { taskWatcherRepository.addWatcher(testTaskId, testAssigneeId, NotificationLevel.ALL) }
    }

    @Test
    fun `assignTask - should unassign task`() = runBlocking {
        // Arrange
        val task = createTestTask(assigneeId = testAssigneeId)
        val member = createTestProjectMember(role = ProjectRole.ADMIN)

        coEvery { taskRepository.findById(testTaskId) } returns task
        coEvery { projectMemberRepository.findByProjectAndUser(testProjectId, testUserId) } returns member
        coEvery { taskRepository.update(any()) } answers { firstArg() }

        // Act
        val result = taskService.assignTask(testTaskId, testUserId, null)

        // Assert
        assertTrue(result.isSuccess)
        assertNull(result.getOrNull()?.assigneeId)

        coVerify(exactly = 1) { taskRepository.update(any()) }
    }

    // ============== Тесты getTaskHistory ==============

    @Test
    fun `getTaskHistory - should return task history`() = runBlocking {
        // Arrange
        val task = createTestTask()
        val history = listOf(
            createTestTaskHistory(),
            createTestTaskHistory()
        )

        coEvery { taskRepository.findById(testTaskId) } returns task
        coEvery { projectMemberRepository.exists(testProjectId, testUserId) } returns true
        coEvery { taskHistoryRepository.findByTask(testTaskId, 20, 0) } returns history

        // Act
        val result = taskService.getTaskHistory(testTaskId, testUserId, 1, 20)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull()?.size)
    }

    @Test
    fun `getTaskHistory - should handle pagination`() = runBlocking {
        // Arrange
        val task = createTestTask()

        coEvery { taskRepository.findById(testTaskId) } returns task
        coEvery { projectMemberRepository.exists(testProjectId, testUserId) } returns true

        // Act
        taskService.getTaskHistory(testTaskId, testUserId, 2, 10) // page 2, size 10 -> offset 10

        // Assert
        coVerify(exactly = 1) { taskHistoryRepository.findByTask(testTaskId, 10, 10) }
    }

    // ============== Тесты getTaskPermissions ==============

    @Test
    fun `getTaskPermissions - should return permissions for owner`() = runBlocking {
        // Arrange
        val task = createTestTask()
        val member = createTestProjectMember(role = ProjectRole.OWNER)

        coEvery { taskRepository.findById(testTaskId) } returns task
        coEvery { projectMemberRepository.exists(testProjectId, testUserId) } returns true
        coEvery { projectMemberRepository.findByProjectAndUser(testProjectId, testUserId) } returns member

        // Act
        val result = taskService.getTaskPermissions(testTaskId, testUserId)

        // Assert
        assertTrue(result.isSuccess)
        val permissions = result.getOrNull()
        assertTrue(permissions?.canEdit == true)
        assertTrue(permissions?.canDelete == true)
        assertTrue(permissions?.canAssign == true)
    }

    @Test
    fun `getTaskPermissions - should return permissions for assignee`() = runBlocking {
        // Arrange
        val task = createTestTask(assigneeId = testUserId) // Пользователь - исполнитель
        val member = createTestProjectMember(role = ProjectRole.MEMBER)

        coEvery { taskRepository.findById(testTaskId) } returns task
        coEvery { projectMemberRepository.exists(testProjectId, testUserId) } returns true
        coEvery { projectMemberRepository.findByProjectAndUser(testProjectId, testUserId) } returns member

        // Act
        val result = taskService.getTaskPermissions(testTaskId, testUserId)

        // Assert
        assertTrue(result.isSuccess)
        val permissions = result.getOrNull()
        assertTrue(permissions?.canEdit == true) // Исполнитель может редактировать
        assertTrue(permissions?.canChangeStatus == true) // Исполнитель может менять статус
        assertFalse(permissions?.canDelete == true) // Но не может удалять
    }

    // ============== Вспомогательные методы ==============

    private fun createTestProject(): Project = Project(
        id = testProjectId,
        companyId = UUID.randomUUID(),
        type = ProjectType.TEAM_MANAGED,
        name = "Test Project",
        key = "TEST",
        description = null,
        status = ProjectStatus.ACTIVE,
        priority = com.quadro.domain.models.project.ProjectPriority.MEDIUM,
        visibility = ProjectVisibility.RESTRICTED,
        leadId = testUserId,
        ownerId = testUserId,
        settings = com.quadro.domain.models.project.ProjectSettings(),
        startDate = null,
        endDate = null,
        completedAt = null,
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis(),
        archivedAt = null
    )

    private fun createTestProjectMember(role: ProjectRole): ProjectMember = ProjectMember(
        id = UUID.randomUUID(),
        projectId = testProjectId,
        userId = testUserId,
        role = role,
        joinedAt = System.currentTimeMillis(),
        invitedBy = testUserId,
        invitedAt = System.currentTimeMillis(),
        sourceTeamId = null
    )

    private fun createTestTask(
        id: UUID = testTaskId,
        parentId: UUID? = null,
        assigneeId: UUID? = testAssigneeId,
        status: TaskStatus = TaskStatus.TODO,
        type: TaskType = TaskType.TASK,
        title: String = "Test Task"
    ): Task = Task(
        id = id,
        projectId = testProjectId,
        parentId = parentId,
        key = testKey,
        title = title,
        description = "Description",
        type = type,
        status = status,
        priority = TaskPriority.MEDIUM,
        resolution = null,
        assigneeId = assigneeId,
        reporterId = testUserId,
        storyPoints = null,
        timeEstimate = null,
        timeSpent = 0,
        dueDate = null,
        startedAt = null,
        completedAt = null,
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis(),
        order = 1,
        tags = null
    )

    private fun createTestTaskHistory(): TaskHistory = TaskHistory(
        id = UUID.randomUUID(),
        taskId = testTaskId,
        userId = testUserId,
        field = HistoryField.STATUS,
        oldValue = "TODO",
        newValue = "IN_PROGRESS",
        createdAt = System.currentTimeMillis()
    )

    private fun createTestUser(): User = User(
        id = testAssigneeId,
        email = "assignee@test.com",
        username = "assignee",
        passwordHash = "hash",
        firstName = "Test",
        lastName = "Assignee",
        role = DomainUserRole.USER,
        isEmailVerified = true,
        isActive = true,
        avatar = null
    )
}