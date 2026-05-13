package com.quadro.task

import com.quadro.task.domain.models.task.Task
import com.quadro.task.domain.models.task.TaskStatus
import com.quadro.task.domain.repositories.task.TaskRepository
import com.quadro.task.domain.services.TaskReportingService
import com.quadro.task.domain.services.TaskReportingServiceImpl
import io.mockk.coEvery
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.Before
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days

@OptIn(ExperimentalCoroutinesApi::class)
class TaskReportingServiceImplTest {
    private lateinit var taskRepository: TaskRepository
    private lateinit var reportingService: TaskReportingService

    private val testProjectId = UUID.randomUUID()
    private val now = Clock.System.now()

    @Before
    fun setUp() {
        taskRepository = mockk()
        reportingService = TaskReportingServiceImpl(taskRepository)
    }

    @Test
    fun `getBacklogCount - returns count`() = runBlocking {
        coEvery { taskRepository.countByStatus(testProjectId, TaskStatus.BACKLOG) } returns 5L
        val result = reportingService.getBacklogCount(testProjectId)
        assertEquals(5L, result)
    }

    @Test
    fun `getTodoCount - returns count`() = runBlocking {
        coEvery { taskRepository.countByStatus(testProjectId, TaskStatus.TODO) } returns 3L
        val result = reportingService.getTodoCount(testProjectId)
        assertEquals(3L, result)
    }

    @Test
    fun `getInProgressCount - returns count`() = runBlocking {
        coEvery { taskRepository.countByStatus(testProjectId, TaskStatus.IN_PROGRESS) } returns 2L
        val result = reportingService.getInProgressCount(testProjectId)
        assertEquals(2L, result)
    }

    @Test
    fun `getInReviewCount - returns count`() = runBlocking {
        coEvery { taskRepository.countByStatus(testProjectId, TaskStatus.IN_REVIEW) } returns 1L
        val result = reportingService.getInReviewCount(testProjectId)
        assertEquals(1L, result)
    }

    @Test
    fun `getDoneCount - returns count`() = runBlocking {
        coEvery { taskRepository.countByStatus(testProjectId, TaskStatus.DONE) } returns 7L
        val result = reportingService.getDoneCount(testProjectId)
        assertEquals(7L, result)
    }

    @Test
    fun `getCancelledCount - returns count`() = runBlocking {
        coEvery { taskRepository.countByStatus(testProjectId, TaskStatus.CANCELLED) } returns 0L
        val result = reportingService.getCancelledCount(testProjectId)
        assertEquals(0L, result)
    }

    @Test
    fun `getOverdueTasks - returns list`() = runBlocking {
        val tasks = listOf(mockk<Task>())
        coEvery { taskRepository.findOverdue(testProjectId, now) } returns tasks
        val result = reportingService.getOverdueTasks(testProjectId, now)
        assertEquals(tasks, result)
    }

    @Test
    fun `getAverageCompletionDays - returns average`() = runBlocking {
        coEvery { taskRepository.avgCompletionDays(testProjectId) } returns 4.2
        val result = reportingService.getAverageCompletionDays(testProjectId)
        assertEquals(4.2, result)
    }

    @Test
    fun `getCompletionRate - returns percentage`() = runBlocking {
        coEvery { taskRepository.countByProject(testProjectId) } returns 20L
        coEvery { taskRepository.countByStatus(testProjectId, TaskStatus.DONE) } returns 5L
        val result = reportingService.getCompletionRate(testProjectId)
        assertEquals(25.0, result) // (5/20)*100 = 25
    }

    @Test
    fun `getCompletionRate - returns 0 when no tasks`() = runBlocking {
        coEvery { taskRepository.countByProject(testProjectId) } returns 0L
        val result = reportingService.getCompletionRate(testProjectId)
        assertEquals(0.0, result)
    }

    @Test
    fun `getVelocity - returns number of completed tasks in last week`() = runBlocking {
        coEvery { taskRepository.countByStatusAndPeriod(testProjectId, TaskStatus.DONE, any(), any()) } returns 8L
        val result = reportingService.getVelocity(testProjectId)
        assertEquals(8.0, result)
    }
}