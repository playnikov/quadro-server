package com.quadro.task

import com.quadro.shared.dto.DomainException
import com.quadro.task.domain.models.task.Task
import com.quadro.task.domain.models.task.TaskPriority
import com.quadro.task.domain.models.task.TaskStatus
import com.quadro.task.domain.models.task.TaskType
import com.quadro.task.domain.repositories.task.TaskRepository
import com.quadro.task.domain.services.TaskStatusService
import com.quadro.task.domain.services.TaskStatusServiceImpl
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.time.Clock

@OptIn(ExperimentalCoroutinesApi::class)
class TaskStatusServiceImplTest {

    private lateinit var taskRepository: TaskRepository
    private lateinit var statusService: TaskStatusService

    private val testProjectId = UUID.randomUUID()
    private val testTaskId = UUID.randomUUID()
    private val testUserId = UUID.randomUUID()
    private val now = Clock.System.now()

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
    fun setUp() {
        taskRepository = mockk(relaxed = true)
        statusService = TaskStatusServiceImpl(taskRepository)
    }

    @Test
    fun `transitionStatus - valid transition`() = runTest {
        val task = testTask.copy(status = TaskStatus.TODO)
        coEvery { taskRepository.findById(testTaskId) } returns task
        coEvery { taskRepository.update(any()) } answers { firstArg() }

        val result = statusService.transitionStatus(testTaskId, TaskStatus.IN_PROGRESS)

        assertEquals(TaskStatus.IN_PROGRESS, result.status)
        assertNotNull(result.startedAt)
    }

    @Test
    fun `transitionStatus - invalid transition`() = runTest {
        val task = testTask.copy(status = TaskStatus.DONE)
        coEvery { taskRepository.findById(testTaskId) } returns task

        val ex = assertFailsWith<DomainException.InvalidTransition> {
            statusService.transitionStatus(testTaskId, TaskStatus.BACKLOG)
        }
        assertEquals("Invalid status transition: DONE → BACKLOG", ex.message)
    }

    @Test
    fun `transitionStatus - task not found`() = runTest {
        coEvery { taskRepository.findById(testTaskId) } returns null

        val ex = assertFailsWith<DomainException.NotFound> {
            statusService.transitionStatus(testTaskId, TaskStatus.IN_PROGRESS)
        }
        assertEquals("Task with '$testTaskId' not found", ex.message)
    }

    // ==================== validateStatusTransition ====================

    @Test
    fun `validateStatusTransition - returns true for valid`() = runTest {
        val task = testTask.copy(status = TaskStatus.TODO)
        coEvery { taskRepository.findById(testTaskId) } returns task
        val result = statusService.validateStatusTransition(testTaskId, TaskStatus.IN_PROGRESS)
        assertEquals(true, result)
    }

    @Test
    fun `validateStatusTransition - returns false for invalid`() = runTest {
        val task = testTask.copy(status = TaskStatus.DONE)
        coEvery { taskRepository.findById(testTaskId) } returns task
        val result = statusService.validateStatusTransition(testTaskId, TaskStatus.BACKLOG)
        assertEquals(false, result)
    }

    @Test
    fun `validateStatusTransition - returns false when task not found`() = runTest {
        coEvery { taskRepository.findById(testTaskId) } returns null
        val result = statusService.validateStatusTransition(testTaskId, TaskStatus.IN_PROGRESS)
        assertEquals(false, result)
    }

    // ==================== startTask ====================

    @Test
    fun `startTask - success`() = runTest {
        val task = testTask.copy(status = TaskStatus.TODO)
        coEvery { taskRepository.findById(testTaskId) } returns task
        coEvery { taskRepository.update(any()) } answers { firstArg() }

        val result = statusService.startTask(testTaskId)

        assertEquals(TaskStatus.IN_PROGRESS, result.status)
    }

    // ==================== completeTask ====================

    @Test
    fun `completeTask - success`() = runTest {
        val task = testTask.copy(
            status = TaskStatus.IN_REVIEW,
            estimatedHours = 5.0
        )
        coEvery { taskRepository.findById(testTaskId) } returns task
        coEvery { taskRepository.update(any()) } answers { firstArg() }

        val result = statusService.completeTask(testTaskId)

        assertEquals(TaskStatus.DONE, result.status)
        assertNotNull(result.completedAt)
    }

    @Test
    fun `completeTask - fails without estimated hours`() = runTest {
        val task = testTask.copy(
            status = TaskStatus.IN_PROGRESS,
            estimatedHours = null
        )
        coEvery { taskRepository.findById(testTaskId) } returns task

        val ex = assertFailsWith<DomainException.BusinessRule> {
            statusService.completeTask(testTaskId)
        }
        assertEquals("Cannot complete task without estimated hours", ex.message)
    }

    // ==================== cancelTask ====================

    @Test
    fun `cancelTask - success`() = runTest {
        val task = testTask.copy(status = TaskStatus.TODO)
        coEvery { taskRepository.findById(testTaskId) } returns task
        coEvery { taskRepository.update(any()) } answers { firstArg() }

        val result = statusService.cancelTask(testTaskId)

        assertEquals(TaskStatus.CANCELLED, result.status)
    }

    // ==================== reopenTask ====================

    @Test
    fun `reopenTask - from DONE to IN_PROGRESS`() = runTest {
        val task = testTask.copy(status = TaskStatus.DONE)
        coEvery { taskRepository.findById(testTaskId) } returns task
        coEvery { taskRepository.update(any()) } answers { firstArg() }

        val result = statusService.reopenTask(testTaskId)

        assertEquals(TaskStatus.IN_PROGRESS, result.status)
    }

    @Test
    fun `reopenTask - from CANCELLED to BACKLOG`() = runTest {
        val task = testTask.copy(status = TaskStatus.CANCELLED)
        coEvery { taskRepository.findById(testTaskId) } returns task
        coEvery { taskRepository.update(any()) } answers { firstArg() }

        val result = statusService.reopenTask(testTaskId)

        assertEquals(TaskStatus.BACKLOG, result.status)
    }

    @Test
    fun `reopenTask - invalid status`() = runTest {
        val task = testTask.copy(status = TaskStatus.IN_PROGRESS)
        coEvery { taskRepository.findById(testTaskId) } returns task

        val ex = assertFailsWith<DomainException.BusinessRule> {
            statusService.reopenTask(testTaskId)
        }
        assertEquals("Task can only be reopened from DONE or CANCELLED status", ex.message)
    }
}