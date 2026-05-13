package com.quadro.task

import com.quadro.shared.dto.DomainException
import com.quadro.task.domain.models.project.Project
import com.quadro.task.domain.models.project.ProjectStatus
import com.quadro.task.domain.models.task.Task
import com.quadro.task.domain.models.task.TaskCreate
import com.quadro.task.domain.models.task.TaskPriority
import com.quadro.task.domain.models.task.TaskStatus
import com.quadro.task.domain.models.task.TaskType
import com.quadro.task.domain.models.task.TaskUpdate
import com.quadro.task.domain.repositories.project.ProjectMemberRepository
import com.quadro.task.domain.repositories.project.ProjectRepository
import com.quadro.task.domain.repositories.task.TaskRepository
import com.quadro.task.domain.repositories.team.TeamProjectRepository
import com.quadro.task.domain.services.TaskService
import com.quadro.task.domain.services.TaskServiceImpl
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.Before
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days

@OptIn(ExperimentalCoroutinesApi::class)
class TaskServiceImplTest {
    private lateinit var taskRepository: TaskRepository
    private lateinit var projectRepository: ProjectRepository
    private lateinit var projectMemberRepository: ProjectMemberRepository
    private lateinit var teamProjectRepository: TeamProjectRepository
    private lateinit var taskService: TaskService

    private val testProjectId = UUID.randomUUID()
    private val testTaskId = UUID.randomUUID()
    private val testUserId = UUID.randomUUID()
    private val testAssigneeId = UUID.randomUUID()
    private val testTeamId = UUID.randomUUID()
    private val now = Clock.System.now()

    private val testProject = Project(
        id = testProjectId,
        key = "TRP",
        status = ProjectStatus.ACTIVE
    )

    private val testTask = Task(
        id = testTaskId,
        projectId = testProjectId,
        sprintId = null,
        parentTaskId = null,
        number = 1,
        title = "Test Task",
        description = null,
        status = TaskStatus.BACKLOG,
        priority = TaskPriority.LOW,
        type = TaskType.TASK,
        assigneeId = null,
        assignedTeamId = null,
        reporterId = testUserId,
        storyPoints = null,
        estimatedHours = null,
        loggedHours = null,
        dueDate = null,
        startedAt = null,
        completedAt = null,
        createdAt = now,
        updatedAt = now,
        labels = emptyList()
    )

    @Before
    fun setup() {
        taskRepository = mockk(relaxed = true)
        projectRepository = mockk(relaxed = true)
        projectMemberRepository = mockk(relaxed = true)
        teamProjectRepository = mockk(relaxed = true)
        taskService = TaskServiceImpl(
            taskRepository,
            projectRepository,
            projectMemberRepository,
            teamProjectRepository
        )
    }

    @Test
    fun `createTask - success without assignee and team`() = runBlocking {
        val create = TaskCreate(
            projectId = testProjectId,
            title = "New Task",
            type = TaskType.TASK,
            priority = TaskPriority.MEDIUM
        )
        coEvery { projectRepository.findById(testProjectId) } returns testProject
        coEvery { taskRepository.nextNumber(testProjectId) } returns 5
        coEvery { taskRepository.create(any()) } answers { firstArg() }

        val result = taskService.createTask(create, testUserId)

        assertEquals(create.title, result.title)
        assertEquals(TaskStatus.BACKLOG, result.status)
        coVerify { taskRepository.create(any()) }
    }

    @Test
    fun `createTask - with assignee valid`() = runBlocking {
        val create = TaskCreate(
            projectId = testProjectId,
            title = "Task",
            type = TaskType.TASK,
            priority = TaskPriority.MEDIUM,
            assigneeId = testAssigneeId
        )
        coEvery { projectRepository.findById(testProjectId) } returns testProject
        coEvery { projectMemberRepository.findByProjectAndUser(testProjectId, testAssigneeId) } returns mockk()
        coEvery { taskRepository.nextNumber(testProjectId) } returns 1
        coEvery { taskRepository.create(any()) } answers { firstArg() }

        val result = taskService.createTask(create, testUserId)

        assertEquals(testAssigneeId, result.assigneeId)
    }

    @Test
    fun `createTask - with assignee not in project`() = runBlocking {
        val create = TaskCreate(
            projectId = testProjectId,
            title = "Task",
            type = TaskType.TASK,
            priority = TaskPriority.MEDIUM,
            assigneeId = testAssigneeId
        )
        coEvery { projectRepository.findById(testProjectId) } returns testProject
        coEvery { projectMemberRepository.findByProjectAndUser(testProjectId, testAssigneeId) } returns null

        val ex = assertFailsWith<DomainException.BusinessRule> {
            taskService.createTask(create, testUserId)
        }
        assertEquals("User is not a member of the project team", ex.message)
    }

    @Test
    fun `createTask - with team valid`() = runBlocking {
        val create = TaskCreate(
            projectId = testProjectId,
            title = "Task",
            type = TaskType.TASK,
            priority = TaskPriority.MEDIUM,
            assignedTeamId = testTeamId
        )
        coEvery { projectRepository.findById(testProjectId) } returns testProject
        coEvery { teamProjectRepository.findByTeamAndProject(testTeamId, testProjectId) } returns mockk()
        coEvery { taskRepository.nextNumber(testProjectId) } returns 1
        coEvery { taskRepository.create(any()) } answers { firstArg() }

        val result = taskService.createTask(create, testUserId)

        assertEquals(testTeamId, result.assignedTeamId)
    }

    @Test
    fun `createTask - fails when project not found`() = runBlocking {
        val create = TaskCreate(
            projectId = testProjectId,
            title = "Task",
            type = TaskType.TASK,
            priority = TaskPriority.MEDIUM,
            assignedTeamId = testTeamId
        )
        coEvery { projectRepository.findById(any()) } returns null

        val ex = assertFailsWith<DomainException.NotFound> {
            taskService.createTask(create, testUserId)
        }
        assertEquals("Project with id '${create.projectId}' not found", ex.message)
    }

    // ==================== updateTask ====================

    @Test
    fun `updateTask - success`() = runBlocking {
        val update = TaskUpdate(title = "Updated Title")
        coEvery { taskRepository.findById(testTaskId) } returns testTask
        coEvery { taskRepository.update(any()) } answers { firstArg() }

        val result = taskService.updateTask(testTaskId, update)

        assertEquals("Updated Title", result.title)
    }

    @Test
    fun `updateTask - fails when task not found`() = runBlocking {
        val update = mockk<TaskUpdate>()
        coEvery { taskRepository.findById(testTaskId) } returns null

        val ex = assertFailsWith<IllegalArgumentException> {
            taskService.updateTask(testTaskId, update)
        }
        assertEquals("Task not found with id: $testTaskId", ex.message)
    }

    // ==================== deleteTask ====================

    @Test
    fun `deleteTask - success`() = runBlocking {
        taskService.deleteTask(testTaskId)
        coVerify { taskRepository.delete(testTaskId) }
    }

    // ==================== getTask ====================

    @Test
    fun `getTask - returns task when exists`() = runBlocking {
        coEvery { taskRepository.findById(testTaskId) } returns testTask
        val result = taskService.getTask(testTaskId)
        assertEquals(testTask, result)
    }

    @Test
    fun `getTask - returns null when not found`() = runBlocking {
        coEvery { taskRepository.findById(testTaskId) } returns null
        val result = taskService.getTask(testTaskId)
        assertNull(result)
    }

    // ==================== getTasksByProject ====================

    @Test
    fun `getTasksByProject - returns list`() = runBlocking {
        val tasks = listOf(testTask)
        coEvery { taskRepository.findByProject(testProjectId, 10, 0) } returns tasks
        val result = taskService.getTasksByProject(testProjectId, 10, 0)
        assertEquals(tasks, result)
    }

    // ==================== getTasksBySprint ====================

    @Test
    fun `getTasksBySprint - returns list`() = runBlocking {
        val sprintId = UUID.randomUUID()
        val tasks = listOf(testTask)
        coEvery { taskRepository.findBySprint(sprintId) } returns tasks
        val result = taskService.getTasksBySprint(sprintId)
        assertEquals(tasks, result)
    }

    // ==================== getTasksByAssignee ====================

    @Test
    fun `getTasksByAssignee - returns list`() = runBlocking {
        val tasks = listOf(testTask)
        coEvery { taskRepository.findByAssignee(testUserId) } returns tasks
        val result = taskService.getTasksByAssignee(testUserId)
        assertEquals(tasks, result)
    }

    // ==================== getNextTaskNumber ====================

    @Test
    fun `getNextTaskNumber - returns number`() = runBlocking {
        coEvery { taskRepository.nextNumber(testProjectId) } returns 42
        val result = taskService.getNextTaskNumber(testProjectId)
        assertEquals(42, result)
    }

    // ==================== countTasksByProject ====================

    @Test
    fun `countTasksByProject - returns count`() = runBlocking {
        coEvery { taskRepository.countByProject(testProjectId) } returns 10L
        val result = taskService.countTasksByProject(testProjectId)
        assertEquals(10L, result)
    }

    // ==================== countTasksByStatus ====================

    @Test
    fun `countTasksByStatus - returns count`() = runBlocking {
        coEvery { taskRepository.countByStatus(testProjectId, TaskStatus.DONE) } returns 5L
        val result = taskService.countTasksByStatus(testProjectId, TaskStatus.DONE)
        assertEquals(5L, result)
    }

    // ==================== countTasksByStatusAndPeriod ====================

    @Test
    fun `countTasksByStatusAndPeriod - returns count`() = runBlocking {
        val from = Clock.System.now().minus(7.days)
        val to = Clock.System.now()
        coEvery { taskRepository.countByStatusAndPeriod(testProjectId, TaskStatus.DONE, from, to) } returns 3L
        val result = taskService.countTasksByStatusAndPeriod(testProjectId, TaskStatus.DONE, from, to)
        assertEquals(3L, result)
    }

    // ==================== findOverdueTasks ====================

    @Test
    fun `findOverdueTasks - returns list`() = runBlocking {
        val now = Clock.System.now()
        val tasks = listOf(testTask)
        coEvery { taskRepository.findOverdue(testProjectId, now) } returns tasks
        val result = taskService.findOverdueTasks(testProjectId, now)
        assertEquals(tasks, result)
    }

    // ==================== getAverageCompletionDays ====================

    @Test
    fun `getAverageCompletionDays - returns average`() = runBlocking {
        coEvery { taskRepository.avgCompletionDays(testProjectId) } returns 3.5
        val result = taskService.getAverageCompletionDays(testProjectId)
        assertEquals(3.5, result)
    }
}