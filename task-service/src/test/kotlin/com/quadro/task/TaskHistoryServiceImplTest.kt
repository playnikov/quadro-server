package com.quadro.task

import com.quadro.task.domain.models.task.HistoryAction
import com.quadro.task.domain.models.task.Task
import com.quadro.task.domain.models.task.TaskHistory
import com.quadro.task.domain.models.task.TaskPriority
import com.quadro.task.domain.models.task.TaskStatus
import com.quadro.task.domain.models.task.TaskType
import com.quadro.task.domain.repositories.task.TaskHistoryRepository
import com.quadro.task.domain.services.TaskHistoryServiceImpl
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Before
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.time.Clock

class TaskHistoryServiceImplTest {
    private lateinit var historyRepository: TaskHistoryRepository
    private lateinit var historyService: TaskHistoryServiceImpl

    private val taskId = UUID.randomUUID()
    private val userId = UUID.randomUUID()
    private val commentId = UUID.randomUUID()

    @Before
    fun setUp() {
        historyRepository = mockk()
        historyService = TaskHistoryServiceImpl(historyRepository)
    }

    private fun createTask(): Task {
        return Task(
            id = taskId,
            projectId = UUID.randomUUID(),
            sprintId = null,
            parentTaskId = null,
            number = 1,
            title = "Test task",
            description = null,
            status = TaskStatus.TODO,
            priority = TaskPriority.MEDIUM,
            type = TaskType.TASK,
            assigneeId = null,
            reporterId = UUID.randomUUID(),
            storyPoints = null,
            estimatedHours = null,
            loggedHours = 0.0,
            dueDate = null,
            startedAt = null,
            completedAt = null,
            createdAt = Clock.System.now(),
            updatedAt = Clock.System.now(),
            labels = emptyList()
        )
    }

    @Test
    fun `recordTaskCreate creates history with CREATE action`() = runTest {
        val task = createTask()
        val historySlot = slot<TaskHistory>()
        coEvery { historyRepository.create(capture(historySlot)) } answers { historySlot.captured }

        historyService.recordTaskCreate(task, userId)

        val history = historySlot.captured
        assertEquals(taskId, history.taskId)
        assertEquals(userId, history.userId)
        assertEquals(HistoryAction.CREATE, history.action)
        assertEquals(null, history.oldValue)
        assertEquals(task.title, history.newValue)
        assertNotNull(history.createdAt)
        coVerify(exactly = 1) { historyRepository.create(any()) }
    }

    @Test
    fun `recordStatusChange creates history with STATUS_CHANGE action`() = runTest {
        val oldStatus = "TODO"
        val newStatus = "IN_PROGRESS"
        val historySlot = slot<TaskHistory>()
        coEvery { historyRepository.create(capture(historySlot)) } answers { historySlot.captured }

        historyService.recordStatusChange(taskId, userId, oldStatus, newStatus)

        val history = historySlot.captured
        assertEquals(taskId, history.taskId)
        assertEquals(userId, history.userId)
        assertEquals(HistoryAction.STATUS_CHANGE, history.action)
        assertEquals(oldStatus, history.oldValue)
        assertEquals(newStatus, history.newValue)
    }

    @Test
    fun `recordAssigneeChange creates history with ASSIGNEE_CHANGE action`() = runTest {
        val oldAssignee = UUID.randomUUID()
        val newAssignee = UUID.randomUUID()
        val historySlot = slot<TaskHistory>()
        coEvery { historyRepository.create(capture(historySlot)) } answers { historySlot.captured }

        historyService.recordAssigneeChange(taskId, userId, oldAssignee, newAssignee)

        val history = historySlot.captured
        assertEquals(HistoryAction.ASSIGNEE_CHANGE, history.action)
        assertEquals(oldAssignee.toString(), history.oldValue)
        assertEquals(newAssignee.toString(), history.newValue)
    }

    @Test
    fun `recordAssigneeChange handles null assignees`() = runTest {
        val historySlot = slot<TaskHistory>()
        coEvery { historyRepository.create(capture(historySlot)) } answers { historySlot.captured }

        historyService.recordAssigneeChange(taskId, userId, null, null)

        val history = historySlot.captured
        assertEquals(null, history.oldValue)
        assertEquals(null, history.newValue)
    }

    @Test
    fun `recordSprintChange creates history with SPRINT_CHANGE action`() = runTest {
        val oldSprint = UUID.randomUUID()
        val newSprint = UUID.randomUUID()
        val historySlot = slot<TaskHistory>()
        coEvery { historyRepository.create(capture(historySlot)) } answers { historySlot.captured }

        historyService.recordSprintChange(taskId, userId, oldSprint, newSprint)

        val history = historySlot.captured
        assertEquals(HistoryAction.SPRINT_CHANGE, history.action)
        assertEquals(oldSprint.toString(), history.oldValue)
        assertEquals(newSprint.toString(), history.newValue)
    }

    @Test
    fun `recordPriorityChange creates history with PRIORITY_CHANGE action`() = runTest {
        val oldPriority = "LOW"
        val newPriority = "HIGH"
        val historySlot = slot<TaskHistory>()
        coEvery { historyRepository.create(capture(historySlot)) } answers { historySlot.captured }

        historyService.recordPriorityChange(taskId, userId, oldPriority, newPriority)

        val history = historySlot.captured
        assertEquals(HistoryAction.PRIORITY_CHANGE, history.action)
        assertEquals(oldPriority, history.oldValue)
        assertEquals(newPriority, history.newValue)
    }

    @Test
    fun `recordCommentAdded creates history with COMMENT_ADDED action`() = runTest {
        val historySlot = slot<TaskHistory>()
        coEvery { historyRepository.create(capture(historySlot)) } answers { historySlot.captured }

        historyService.recordCommentAdded(taskId, userId, commentId)

        val history = historySlot.captured
        assertEquals(HistoryAction.COMMENT_ADDED, history.action)
        assertEquals(null, history.oldValue)
        assertEquals(commentId.toString(), history.newValue)
    }

    @Test
    fun `getHistory delegates to repository`() = runTest {
        val limit = 10
        val offset = 0
        val expectedHistory = listOf(mockk<TaskHistory>())
        coEvery { historyRepository.findByTask(taskId, limit, offset) } returns expectedHistory

        val result = historyService.getHistory(taskId, limit, offset)

        assertEquals(expectedHistory, result)
        coVerify { historyRepository.findByTask(taskId, limit, offset) }
    }
}