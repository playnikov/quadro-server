package com.quadro.task

import com.quadro.shared.data.messaging.EventProducer
import com.quadro.shared.data.messaging.KafkaTopics
import com.quadro.shared.data.messaging.events.TaskAssignedEvent
import com.quadro.shared.dto.DomainException
import com.quadro.task.domain.models.project.MemberRole
import com.quadro.task.domain.models.project.ProjectMember
import com.quadro.task.domain.models.task.Task
import com.quadro.task.domain.models.task.TaskPriority
import com.quadro.task.domain.models.task.TaskStatus
import com.quadro.task.domain.models.task.TaskType
import com.quadro.task.domain.repositories.project.ProjectMemberRepository
import com.quadro.task.domain.repositories.task.TaskRepository
import com.quadro.task.domain.services.TaskAssignmentServiceImpl
import com.quadro.task.domain.services.TaskHistoryService
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Before
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.time.Clock

class TaskAssignmentServiceImplTest {
    private lateinit var taskRepository: TaskRepository
    private lateinit var taskHistoryService: TaskHistoryService
    private lateinit var projectMemberRepository: ProjectMemberRepository
    private lateinit var assignmentService: TaskAssignmentServiceImpl

    private val testProjectId = UUID.randomUUID()
    private val testTaskId = UUID.randomUUID()
    private val assignerId = UUID.randomUUID()   // тот, кто назначает
    private val assigneeId = UUID.randomUUID()   // кого назначают
    private val managerMember = mockk<ProjectMember> {
        every { role } returns MemberRole.MANAGER
    }

    @Before
    fun setUp() {
        taskRepository = mockk()
        taskHistoryService = mockk()
        projectMemberRepository = mockk()
        assignmentService = TaskAssignmentServiceImpl(
            taskRepository,
            taskHistoryService,
            projectMemberRepository,
            mockk(relaxed = true)
        )
    }

    private fun createTask(assigneeId: UUID? = null): Task {
        return Task(
            id = testTaskId,
            projectId = testProjectId,
            sprintId = null,
            parentTaskId = null,
            number = 1,
            title = "Test task",
            description = null,
            status = TaskStatus.TODO,
            priority = TaskPriority.MEDIUM,
            type = TaskType.TASK,
            assigneeId = assigneeId,
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
    fun `assignTaskToUser throws when task not found`() = runTest {
        coEvery { taskRepository.findById(testTaskId) } returns null

        assertFailsWith<DomainException.NotFound> {
            assignmentService.assignTaskToUser(testTaskId, assigneeId, assignerId)
        }
    }

    @Test
    fun `assignTaskToUser checks permissions when task already has assignee`() = runTest {
        val existingAssignee = UUID.randomUUID()
        val task = createTask(assigneeId = existingAssignee)
        coEvery { taskRepository.findById(testTaskId) } returns task

        coEvery { projectMemberRepository.findByProjectAndUser(testProjectId, assignerId) } returns null

        assertFailsWith<DomainException.AccessDenied> {
            assignmentService.assignTaskToUser(testTaskId, assigneeId, assignerId)
        }
        coVerify { projectMemberRepository.findByProjectAndUser(testProjectId, assignerId) }
    }

    @Test
    fun `assignTaskToUser checks permissions when assigning to different user`() = runTest {
        val task = createTask(assigneeId = null)
        coEvery { taskRepository.findById(testTaskId) } returns task
        coEvery { projectMemberRepository.findByProjectAndUser(testProjectId, assignerId) } returns null

        assertFailsWith<DomainException.AccessDenied> {
            assignmentService.assignTaskToUser(testTaskId, assigneeId, assignerId)
        }
    }

    @Test
    fun `assignTaskToUser allows self-assignment without permissions`() = runTest {
        val task = createTask(assigneeId = null)
        coEvery { taskRepository.findById(testTaskId) } returns task
        val updatedTaskSlot = slot<Task>()
        coEvery { taskRepository.update(capture(updatedTaskSlot)) } answers { updatedTaskSlot.captured }
        coEvery { taskHistoryService.recordAssigneeChange(any(), any(), any(), any()) } just Runs

        val result = assignmentService.assignTaskToUser(testTaskId, assignerId, assignerId)

        assertEquals(assignerId, result.assigneeId)
        coVerify(exactly = 0) { projectMemberRepository.findByProjectAndUser(any(), any()) }
        coVerify { taskRepository.update(any()) }
        coVerify { taskHistoryService.recordAssigneeChange(testTaskId, assignerId, null, assignerId) }
    }

    @Test
    fun `assignTaskToUser successful assignment by manager`() = runTest {
        val task = createTask(assigneeId = null)
        coEvery { taskRepository.findById(testTaskId) } returns task
        coEvery { projectMemberRepository.findByProjectAndUser(testProjectId, assignerId) } returns managerMember
        val updatedTaskSlot = slot<Task>()
        coEvery { taskRepository.update(capture(updatedTaskSlot)) } answers { updatedTaskSlot.captured }
        coEvery { taskHistoryService.recordAssigneeChange(any(), any(), any(), any()) } just Runs

        val result = assignmentService.assignTaskToUser(testTaskId, assigneeId, assignerId)

        assertEquals(assigneeId, result.assigneeId)
        coVerify { taskRepository.update(any()) }
        coVerify { taskHistoryService.recordAssigneeChange(testTaskId, assignerId, null, assigneeId) }
    }

    @Test
    fun `unassignTask throws when task not found`() = runTest {
        coEvery { taskRepository.findById(testTaskId) } returns null

        assertFailsWith<IllegalArgumentException> {
            assignmentService.unassignTask(testTaskId, assignerId)
        }
    }

    @Test
    fun `unassignTask checks permissions`() = runTest {
        val task = createTask(assigneeId = assigneeId)
        coEvery { taskRepository.findById(testTaskId) } returns task
        coEvery { projectMemberRepository.findByProjectAndUser(testProjectId, assignerId) } returns null

        assertFailsWith<DomainException.AccessDenied> {
            assignmentService.unassignTask(testTaskId, assignerId)
        }
    }

    @Test
    fun `unassignTask successful unassignment`() = runTest {
        val task = createTask(assigneeId = assigneeId)
        coEvery { taskRepository.findById(testTaskId) } returns task
        coEvery { projectMemberRepository.findByProjectAndUser(testProjectId, assignerId) } returns managerMember
        val updatedTaskSlot = slot<Task>()
        coEvery { taskRepository.update(capture(updatedTaskSlot)) } answers { updatedTaskSlot.captured }
        coEvery { taskHistoryService.recordAssigneeChange(any(), any(), any(), any()) } just Runs

        val result = assignmentService.unassignTask(testTaskId, assignerId)

        assertNull(result.assigneeId)
        coVerify { taskRepository.update(any()) }
        coVerify { taskHistoryService.recordAssigneeChange(testTaskId, assignerId, assigneeId, null) }
    }
}