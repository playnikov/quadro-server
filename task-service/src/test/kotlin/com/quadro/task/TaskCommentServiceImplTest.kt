package com.quadro.task

import com.quadro.shared.dto.DomainException
import com.quadro.task.domain.models.project.MemberRole
import com.quadro.task.domain.models.project.ProjectMember
import com.quadro.task.domain.models.task.Task
import com.quadro.task.domain.models.task.TaskComment
import com.quadro.task.domain.models.task.TaskCommentCreate
import com.quadro.task.domain.models.task.TaskCommentUpdate
import com.quadro.task.domain.models.task.TaskPriority
import com.quadro.task.domain.models.task.TaskStatus
import com.quadro.task.domain.models.task.TaskType
import com.quadro.task.domain.repositories.project.ProjectMemberRepository
import com.quadro.task.domain.repositories.task.TaskCommentRepository
import com.quadro.task.domain.repositories.task.TaskRepository
import com.quadro.task.domain.services.TaskCommentServiceImpl
import com.quadro.task.domain.services.TaskHistoryService
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.Before
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.time.Clock

class TaskCommentServiceImplTest {
    private lateinit var commentRepository: TaskCommentRepository
    private lateinit var taskRepository: TaskRepository
    private lateinit var projectMemberRepository: ProjectMemberRepository
    private lateinit var taskHistoryService: TaskHistoryService
    private lateinit var commentService: TaskCommentServiceImpl

    private val testProjectId = UUID.randomUUID()
    private val testTaskId = UUID.randomUUID()
    private val authorId = UUID.randomUUID()
    private val managerId = UUID.randomUUID()
    private val commentId = UUID.randomUUID()
    private val parentCommentId = UUID.randomUUID()

    private val managerMember = mockk<ProjectMember> {
        every { role } returns MemberRole.MANAGER
    }
    private val regularMember = mockk<ProjectMember> {
        every { role } returns MemberRole.MEMBER
    }

    @Before
    fun setUp() {
        commentRepository = mockk()
        taskRepository = mockk()
        projectMemberRepository = mockk()
        taskHistoryService = mockk()
        commentService = TaskCommentServiceImpl(
            commentRepository,
            taskRepository,
            projectMemberRepository,
            taskHistoryService,
            mockk(relaxed = true)
        )
    }

    private fun createTask(): Task {
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

    private fun createComment(authorId: UUID, isDeleted: Boolean = false, parentId: UUID? = null): TaskComment {
        return TaskComment(
            id = commentId,
            taskId = testTaskId,
            authorId = authorId,
            content = "Original content",
            parentId = parentId,
            isEdited = false,
            isDeleted = isDeleted,
            mentions = emptyList(),
            createdAt = Clock.System.now(),
            updatedAt = Clock.System.now()
        )
    }

    @Test
    fun `createComment throws when task not found`() = runTest {
        coEvery { taskRepository.findById(testTaskId) } returns null

        val commentCreate = TaskCommentCreate(
            taskId = testTaskId,
            authorId = authorId,
            content = "Hello",
            parentId = null,
            mentions = emptyList()
        )

        assertFailsWith<DomainException.NotFound> {
            commentService.createComment(commentCreate)
        }
    }

    @Test
    fun `createComment throws when parent comment not found`() = runTest {
        coEvery { taskRepository.findById(testTaskId) } returns createTask()
        coEvery { commentRepository.findById(parentCommentId) } returns null

        val commentCreate = TaskCommentCreate(
            taskId = testTaskId,
            authorId = authorId,
            content = "Hello",
            parentId = parentCommentId,
            mentions = emptyList()
        )

        assertFailsWith<DomainException.NotFound> {
            commentService.createComment(commentCreate)
        }
    }

    @Test
    fun `createComment throws when parent comment belongs to different task`() = runTest {
        val otherTaskId = UUID.randomUUID()
        coEvery { taskRepository.findById(testTaskId) } returns createTask()
        val parentComment = mockk<TaskComment> {
            every { taskId } returns otherTaskId
        }
        coEvery { commentRepository.findById(parentCommentId) } returns parentComment

        val commentCreate = TaskCommentCreate(
            taskId = testTaskId,
            authorId = authorId,
            content = "Hello",
            parentId = parentCommentId,
            mentions = emptyList()
        )

        assertFailsWith<DomainException.BusinessRule> {
            commentService.createComment(commentCreate)
        }
    }

    @Test
    fun `createComment successful`() = runTest {
        coEvery { taskRepository.findById(testTaskId) } returns createTask()
        val createdCommentSlot = slot<TaskComment>()
        coEvery { commentRepository.create(capture(createdCommentSlot)) } answers { createdCommentSlot.captured }
        coEvery { taskHistoryService.recordCommentAdded(any(), any(), any()) } just Runs

        val commentCreate = TaskCommentCreate(
            taskId = testTaskId,
            authorId = authorId,
            content = "Hello world",
            parentId = null,
            mentions = listOf(UUID.randomUUID())
        )

        val result = commentService.createComment(commentCreate)

        assertEquals(commentCreate.taskId, result.taskId)
        assertEquals(commentCreate.authorId, result.authorId)
        assertEquals(commentCreate.content, result.content)
        assertFalse(result.isEdited)
        assertFalse(result.isDeleted)
        coVerify { commentRepository.create(any()) }
        coVerify { taskHistoryService.recordCommentAdded(testTaskId, authorId, result.id) }
    }

    @Test
    fun `updateComment throws when comment not found`() = runTest {
        coEvery { commentRepository.findById(commentId) } returns null

        val update = TaskCommentUpdate(content = "Updated")

        assertFailsWith<DomainException.NotFound> {
            commentService.updateComment(commentId, authorId, update)
        }
    }

    @Test
    fun `updateComment throws when not author`() = runTest {
        val comment = createComment(authorId = UUID.randomUUID())
        coEvery { commentRepository.findById(commentId) } returns comment

        val update = TaskCommentUpdate(content = "Updated")

        assertFailsWith<DomainException.Forbidden> {
            commentService.updateComment(commentId, authorId, update)
        }
    }

    @Test
    fun `updateComment throws when comment already deleted`() = runTest {
        val comment = createComment(authorId = authorId, isDeleted = true)
        coEvery { commentRepository.findById(commentId) } returns comment

        val update = TaskCommentUpdate(content = "Updated")

        assertFailsWith<DomainException.BusinessRule> {
            commentService.updateComment(commentId, authorId, update)
        }
    }

    @Test
    fun `updateComment successful`() = runTest {
        val comment = createComment(authorId = authorId, isDeleted = false)
        coEvery { commentRepository.findById(commentId) } returns comment
        val updatedCommentSlot = slot<TaskComment>()
        coEvery { commentRepository.update(capture(updatedCommentSlot)) } answers { updatedCommentSlot.captured }

        val update = TaskCommentUpdate(content = "New content")
        val result = commentService.updateComment(commentId, authorId, update)

        assertEquals("New content", result.content)
        assertTrue(result.isEdited)
        coVerify { commentRepository.update(any()) }
        coVerify(exactly = 0) { taskHistoryService.recordCommentAdded(any(), any(), any()) }
    }

    @Test
    fun `deleteComment throws when comment not found`() = runTest {
        coEvery { commentRepository.findById(commentId) } returns null

        assertFailsWith<DomainException.NotFound> {
            commentService.deleteComment(commentId, authorId)
        }
    }

    @Test
    fun `deleteComment throws when not author and not manager`() = runTest {
        val comment = createComment(authorId = UUID.randomUUID())
        coEvery { commentRepository.findById(commentId) } returns comment
        coEvery { taskRepository.findById(testTaskId) } returns createTask()
        coEvery { projectMemberRepository.findByProjectAndUser(testProjectId, authorId) } returns regularMember

        assertFailsWith<DomainException.Forbidden> {
            commentService.deleteComment(commentId, authorId)
        }
    }

    @Test
    fun `deleteComment allows manager to delete any comment`() = runTest {
        val comment = createComment(authorId = UUID.randomUUID())
        coEvery { commentRepository.findById(commentId) } returns comment
        coEvery { taskRepository.findById(testTaskId) } returns createTask()
        coEvery { projectMemberRepository.findByProjectAndUser(testProjectId, managerId) } returns managerMember
        coEvery { commentRepository.softDelete(commentId) } returns true

        commentService.deleteComment(commentId, managerId)

        coVerify { commentRepository.softDelete(commentId) }
    }

    @Test
    fun `getCommentsByTask returns only non-deleted comments`() = runTest {
        val deletedComment = createComment(authorId = authorId, isDeleted = true)
        val activeComment = createComment(authorId = authorId, isDeleted = false)
        coEvery { commentRepository.findByTask(testTaskId) } returns listOf(deletedComment, activeComment)

        val result = commentService.getCommentsByTask(testTaskId)

        assertEquals(1, result.size)
        assertFalse(result[0].isDeleted)
    }

    @Test
    fun `getReplies returns only non-deleted replies`() = runTest {
        val deletedReply = createComment(authorId = authorId, isDeleted = true, parentId = parentCommentId)
        val activeReply = createComment(authorId = authorId, isDeleted = false, parentId = parentCommentId)
        coEvery { commentRepository.findReplies(parentCommentId) } returns listOf(deletedReply, activeReply)

        val result = commentService.getReplies(parentCommentId)

        assertEquals(1, result.size)
        assertFalse(result[0].isDeleted)
    }

    @Test
    fun `countByTask delegates to repository`() = runTest {
        coEvery { commentRepository.countByTask(testTaskId) } returns 5L

        val result = commentService.countByTask(testTaskId)

        assertEquals(5L, result)
    }

    @Test
    fun `getComment delegates to repository`() = runTest {
        val comment = createComment(authorId = authorId)
        coEvery { commentRepository.findById(commentId) } returns comment

        val result = commentService.getComment(commentId)

        assertEquals(comment, result)
    }
}