package com.quadro.task

import com.quadro.shared.dto.DomainException
import com.quadro.task.domain.models.task.Sprint
import com.quadro.task.domain.models.task.SprintCreate
import com.quadro.task.domain.models.task.SprintStatus
import com.quadro.task.domain.models.task.SprintUpdate
import com.quadro.task.domain.repositories.task.SprintRepository
import com.quadro.task.domain.services.SprintService
import com.quadro.task.domain.services.SprintServiceImpl
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
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days

@OptIn(ExperimentalCoroutinesApi::class)
class SprintServiceImplTest {
    private lateinit var sprintRepository: SprintRepository
    private lateinit var sprintService: SprintService

    private val testSprintId = UUID.randomUUID()
    private val testProjectId = UUID.randomUUID()
    private val testUserId = UUID.randomUUID()
    private val now = Clock.System.now()

    private val testSprint = Sprint(
        id = testSprintId,
        projectId = testProjectId,
        name = "Sprint 1",
        goal = "Complete features",
        status = SprintStatus.PLANNING,
        startDate = now,
        endDate = now + 7.days,
        createdBy = testUserId,
        createdAt = now,
        updatedAt = now
    )

    @Before
    fun setUp() {
        sprintRepository = mockk(relaxed = true)
        sprintService = SprintServiceImpl(sprintRepository)
    }

    // ==================== createSprint ====================

    @Test
    fun `createSprint - success with custom dates`() = runBlocking {
        val create = SprintCreate(
            projectId = testProjectId,
            name = "Sprint 2",
            goal = "New goal",
            status = SprintStatus.ACTIVE,
            startDate = now,
            endDate = now + 14.days,
            createdBy = testUserId
        )
        coEvery { sprintRepository.create(any()) } answers { firstArg() }

        val result = sprintService.createSprint(create)

        assertEquals(create.name, result.name)
        assertEquals(create.startDate, result.startDate)
        assertEquals(create.endDate, result.endDate)
    }

    // ==================== updateSprint ====================

    @Test
    fun `updateSprint - success`() = runBlocking {
        val update = SprintUpdate(
            name = "Updated Sprint",
            goal = "New goal",
            status = SprintStatus.COMPLETED,
            startDate = now + 1.days,
            endDate = now + 8.days
        )
        coEvery { sprintRepository.findById(testSprintId) } returns testSprint
        coEvery { sprintRepository.update(any()) } answers { firstArg() }

        val result = sprintService.updateSprint(testSprintId, update)

        assertEquals("Updated Sprint", result.name)
        assertEquals(SprintStatus.COMPLETED, result.status)
    }

    @Test
    fun `updateSprint - fails when sprint not found`() = runBlocking {
        val update = mockk<SprintUpdate>()
        coEvery { sprintRepository.findById(testSprintId) } returns null

        val ex = assertFailsWith<DomainException.NotFound> {
            sprintService.updateSprint(testSprintId, update)
        }
        assertEquals("Sprint with id '$testSprintId' not found", ex.message)
    }

    // ==================== deleteSprint ====================

    @Test
    fun `deleteSprint - success`() = runBlocking {
        coEvery { sprintRepository.findById(testSprintId) } returns testSprint
        sprintService.deleteSprint(testSprintId)
        coVerify { sprintRepository.delete(testSprintId) }
    }

    @Test
    fun `deleteSprint - fails when sprint not found`() = runBlocking {
        coEvery { sprintRepository.findById(testSprintId) } returns null

        val ex = assertFailsWith<DomainException.NotFound> {
            sprintService.deleteSprint(testSprintId)
        }
        assertEquals("Sprint with id '$testSprintId' not found", ex.message)
    }

    // ==================== getSprint ====================

    @Test
    fun `getSprint - returns sprint when exists`() = runBlocking {
        coEvery { sprintRepository.findById(testSprintId) } returns testSprint
        val result = sprintService.getSprint(testSprintId)
        assertEquals(testSprint, result)
    }

    @Test
    fun `getSprint - returns null when not found`() = runBlocking {
        coEvery { sprintRepository.findById(testSprintId) } returns null
        val result = sprintService.getSprint(testSprintId)
        assertNull(result)
    }

    // ==================== getSprintsByProject ====================

    @Test
    fun `getSprintsByProject - returns list`() = runBlocking {
        val sprints = listOf(testSprint)
        coEvery { sprintRepository.findByProjectId(testProjectId) } returns sprints
        val result = sprintService.getSprintsByProject(testProjectId)
        assertEquals(sprints, result)
    }
}