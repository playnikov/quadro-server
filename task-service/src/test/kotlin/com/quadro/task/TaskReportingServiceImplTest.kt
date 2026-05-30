package com.quadro.task

import com.quadro.task.domain.models.task.Task
import com.quadro.task.domain.models.task.TaskStatus
import com.quadro.task.domain.models.task.VelocityMetric
import com.quadro.task.domain.repositories.UserRepository
import com.quadro.task.domain.repositories.project.ProjectMemberRepository
import com.quadro.task.domain.repositories.project.ProjectRepository
import com.quadro.task.domain.repositories.task.TaskRepository
import com.quadro.task.domain.services.TaskReportingService
import com.quadro.task.domain.services.TaskReportingServiceImpl
import io.mockk.coEvery
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Before
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days

@OptIn(ExperimentalCoroutinesApi::class)
class TaskReportingServiceImplTest {
    private lateinit var taskRepository: TaskRepository
    private lateinit var userRepository: UserRepository
    private lateinit var projectRepository: ProjectRepository
    private lateinit var projectMemberRepository: ProjectMemberRepository
    private lateinit var reportingService: TaskReportingServiceImpl

    private val testProjectId = UUID.randomUUID()

    @Before
    fun setUp() {
        taskRepository = mockk()
        userRepository = mockk()
        projectRepository = mockk()
        projectMemberRepository = mockk()
        reportingService = TaskReportingServiceImpl(
            taskRepository,
            userRepository,
            projectRepository,
            projectMemberRepository
        )
    }

    @Test
    fun `getBacklogCount delegates to repository`() = runTest {
        coEvery { taskRepository.countByStatus(testProjectId, TaskStatus.BACKLOG) } returns 42L
        val result = reportingService.getBacklogCount(testProjectId)
        assertEquals(42L, result)
    }

    @Test
    fun `getTodoCount delegates to repository`() = runTest {
        coEvery { taskRepository.countByStatus(testProjectId, TaskStatus.TODO) } returns 7L
        val result = reportingService.getTodoCount(testProjectId)
        assertEquals(7L, result)
    }

    @Test
    fun `getInProgressCount delegates to repository`() = runTest {
        coEvery { taskRepository.countByStatus(testProjectId, TaskStatus.IN_PROGRESS) } returns 3L
        val result = reportingService.getInProgressCount(testProjectId)
        assertEquals(3L, result)
    }

    @Test
    fun `getInReviewCount delegates to repository`() = runTest {
        coEvery { taskRepository.countByStatus(testProjectId, TaskStatus.IN_REVIEW) } returns 2L
        val result = reportingService.getInReviewCount(testProjectId)
        assertEquals(2L, result)
    }

    @Test
    fun `getDoneCount delegates to repository`() = runTest {
        coEvery { taskRepository.countByStatus(testProjectId, TaskStatus.DONE) } returns 10L
        val result = reportingService.getDoneCount(testProjectId)
        assertEquals(10L, result)
    }

    @Test
    fun `getCancelledCount delegates to repository`() = runTest {
        coEvery { taskRepository.countByStatus(testProjectId, TaskStatus.CANCELLED) } returns 1L
        val result = reportingService.getCancelledCount(testProjectId)
        assertEquals(1L, result)
    }

    @Test
    fun `getTaskCounts returns map for all statuses`() = runTest {
        val repoCounts = mapOf(
            TaskStatus.BACKLOG to 5L,
            TaskStatus.TODO to 3L,
            TaskStatus.IN_PROGRESS to 4L,
            TaskStatus.DONE to 8L
        )
        coEvery { taskRepository.countGroupedByStatus(testProjectId) } returns repoCounts

        val result = reportingService.getTaskCounts(testProjectId)

        TaskStatus.entries.forEach { status ->
            val expected = repoCounts[status] ?: 0L
            assertEquals(expected, result[status], "Mismatch for status $status")
        }
    }

    @Test
    fun `getTaskCounts returns zero for missing statuses`() = runTest {
        coEvery { taskRepository.countGroupedByStatus(testProjectId) } returns emptyMap()

        val result = reportingService.getTaskCounts(testProjectId)

        TaskStatus.entries.forEach { status ->
            assertEquals(0L, result[status])
        }
    }

    @Test
    fun `getOverdueCount delegates to repository`() = runTest {
        val now = Clock.System.now()

        coEvery { taskRepository.countOverdue(testProjectId, now) } returns 5L
        val result = reportingService.getOverdueCount(testProjectId, now)
        assertEquals(5L, result)
    }

    @Test
    fun `getAverageCompletionDays with null period delegates`() = runTest {
        coEvery { taskRepository.avgCompletionDaysInPeriod(testProjectId, null, null) } returns 4.2
        val result = reportingService.getAverageCompletionDays(testProjectId, null, null)
        assertEquals(4.2, result)
    }

    @Test
    fun `getAverageCompletionDays with period delegates`() = runTest {
        val now = Clock.System.now()

        val from = now - 30.days
        val to = now
        coEvery { taskRepository.avgCompletionDaysInPeriod(testProjectId, from, to) } returns 3.7
        val result = reportingService.getAverageCompletionDays(testProjectId, from, to)
        assertEquals(3.7, result)
    }

    @Test
    fun `getCompletionRate computes percentage`() = runTest {
        coEvery { taskRepository.countByProject(testProjectId) } returns 100L
        coEvery { taskRepository.countByStatus(testProjectId, TaskStatus.DONE) } returns 75L

        val result = reportingService.getCompletionRate(testProjectId)

        assertEquals(75.0, result)
    }

    @Test
    fun `getVelocity with TASK_COUNT returns count of completed tasks`() = runTest {
        val daysBack = 14L
        coEvery { taskRepository.countCompletedByPeriod(eq(testProjectId), any(), any()) } returns 23L

        val result = reportingService.getVelocity(testProjectId, VelocityMetric.TASK_COUNT, daysBack)

        assertEquals(23.0, result)
    }

    @Test
    fun `getVelocity with STORY_POINTS returns sum of story points`() = runTest {
        val daysBack = 7L
        coEvery { taskRepository.countCompletedByPeriod(eq(testProjectId), any(), any()) } returns 23L

        val result = reportingService.getVelocity(testProjectId, VelocityMetric.TASK_COUNT, daysBack)

        assertEquals(23.0, result)
    }

    @Test
    fun `getVelocity with STORY_POINTS returns zero when no data`() = runTest {
        val daysBack = 7L
        coEvery { taskRepository.sumStoryPointsCompletedInPeriod(eq(testProjectId), any(), any()) } returns null

        val result = reportingService.getVelocity(testProjectId, VelocityMetric.STORY_POINTS, daysBack)

        assertEquals(0.0, result)
    }

    @Test
    fun `getVelocity with ESTIMATED_HOURS returns sum of estimated hours`() = runTest {
        val daysBack = 30L
        coEvery { taskRepository.sumEstimatedHoursCompletedInPeriod(eq(testProjectId), any(), any()) } returns 120.5

        val result = reportingService.getVelocity(testProjectId, VelocityMetric.ESTIMATED_HOURS, daysBack)

        assertEquals(120.5, result)
    }

    @Test
    fun `getVelocity with ESTIMATED_HOURS returns zero when no data`() = runTest {
        val daysBack = 30L
        coEvery { taskRepository.sumEstimatedHoursCompletedInPeriod(eq(testProjectId), any(), any()) } returns null

        val result = reportingService.getVelocity(testProjectId, VelocityMetric.ESTIMATED_HOURS, daysBack)

        assertEquals(0.0, result)
    }

    @Test
    fun `getPeriodReport computes all metrics correctly`() = runTest {
        val now = Clock.System.now()
        val from = now - 6.days
        val to = now
        val daysInPeriod = 7L

        coEvery { taskRepository.countCreatedByPeriod(eq(testProjectId), any(), any()) } returns 20L
        coEvery { taskRepository.countCompletedByPeriod(eq(testProjectId), any(), any()) } returns 15L
        coEvery { taskRepository.countByStatusAndPeriod(any(), any(), any(), any()) } returns 3L
        coEvery { taskRepository.getTasksCreatedGroupedByDay(eq(testProjectId), any(), any()) } returns
                mapOf(Clock.System.now().minus(1.days) to 10L, Clock.System.now() to 10L)
        coEvery { taskRepository.getTasksProgressGroupedByDay(eq(testProjectId), any(), any()) } returns
                mapOf(Clock.System.now().minus(1.days) to 5L, Clock.System.now() to 8L)
        coEvery { taskRepository.getTasksCompletedGroupedByDay(eq(testProjectId), any(), any()) } returns
                mapOf(Clock.System.now() to 15L)
        coEvery { taskRepository.countOverdue(eq(testProjectId), any()) } returns 2L
        coEvery { taskRepository.avgCompletionDaysInPeriod(eq(testProjectId), any(), any()) } returns 4.2
        coEvery { taskRepository.averageWipInPeriod(eq(testProjectId), any(), any()) } returns 6.5

        val report = reportingService.getPeriodReport(testProjectId, from, to)

        assertEquals(from, report.from)
        assertEquals(to, report.to)
        assertEquals(20L, report.created)
        assertEquals(15L, report.completed)
        assertEquals(4.2, report.averageCompletionDays)
        assertEquals(2L, report.overdueCount)
        assertEquals(15.0 / daysInPeriod, report.throughput, 0.001)
        assertEquals((15.0 / 20) * 100, report.efficiency, 0.001)
        assertEquals(6.5, report.wipAverage)

        assertTrue(report.dailyCreation.isNotEmpty())
        assertTrue(report.dailyProgress.isNotEmpty())
        assertTrue(report.dailyCompletion.isNotEmpty())
    }

    @Test
    fun `getPeriodReport handles zero created tasks (efficiency zero)`() = runTest {
        val now = Clock.System.now()
        val from = now - 6.days
        val to = now

        coEvery { taskRepository.countCreatedByPeriod(testProjectId, from, to) } returns 0L
        coEvery { taskRepository.countCompletedByPeriod(testProjectId, from, to) } returns 0L
        coEvery { taskRepository.countByStatusAndPeriod(any(), any(), any(), any()) } returns 0L
        coEvery { taskRepository.getTasksCreatedGroupedByDay(any(), any(), any()) } returns emptyMap()
        coEvery { taskRepository.getTasksProgressGroupedByDay(any(), any(), any()) } returns emptyMap()
        coEvery { taskRepository.getTasksCompletedGroupedByDay(any(), any(), any()) } returns emptyMap()
        coEvery { taskRepository.countOverdue(any(), any()) } returns 0L
        coEvery { taskRepository.avgCompletionDaysInPeriod(any(), any(), any()) } returns 0.0
        coEvery { taskRepository.averageWipInPeriod(any(), any(), any()) } returns 0.0

        val report = reportingService.getPeriodReport(testProjectId, from, to)

        assertEquals(0.0, report.efficiency)
        assertEquals(0.0, report.throughput)
    }

    @Test
    fun `getOverdueTasks delegates paginated`() = runTest {
        val now = Clock.System.now()
        val page = 0
        val size = 20
        val expectedTasks = listOf(mockk<Task>(), mockk<Task>())
        coEvery {
            taskRepository.findOverduePaginated(testProjectId, now, page, size)
        } returns expectedTasks

        val result = reportingService.getOverdueTasks(testProjectId, now, page, size)

        assertEquals(expectedTasks, result)
    }
}