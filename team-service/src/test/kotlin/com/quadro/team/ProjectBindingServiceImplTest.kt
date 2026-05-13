package com.quadro.team

import com.quadro.shared.data.messaging.EventProducer
import com.quadro.shared.dto.DomainException
import com.quadro.team.domain.models.Project
import com.quadro.team.domain.models.ProjectStatus
import com.quadro.team.domain.models.Team
import com.quadro.team.domain.models.TeamProjectBinding
import com.quadro.team.domain.models.TeamProjectRole
import com.quadro.team.domain.models.TeamStatus
import com.quadro.team.domain.models.TeamVisibility
import com.quadro.team.domain.repositories.ProjectRepository
import com.quadro.team.domain.repositories.TeamProjectBindingRepository
import com.quadro.team.domain.repositories.TeamRepository
import com.quadro.team.domain.services.ProjectBindingService
import com.quadro.team.domain.services.ProjectBindingServiceImpl
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
import kotlin.time.Clock

@OptIn(ExperimentalCoroutinesApi::class)
class ProjectBindingServiceImplTest {
    private lateinit var teamRepository: TeamRepository
    private lateinit var projectRepository: ProjectRepository
    private lateinit var bindingRepository: TeamProjectBindingRepository
    private lateinit var eventProducer: EventProducer
    private lateinit var bindingService: ProjectBindingService

    private val testUserId = UUID.randomUUID()
    private val testTeamId = UUID.randomUUID()
    private val testProjectId = UUID.randomUUID()
    private val testRequesterId = UUID.randomUUID()
    private val testBindingId = UUID.randomUUID()
    private val now = Clock.System.now()


    private val testTeam = Team(
        id = testTeamId,
        name = "Test Team",
        description = "Desc",
        avatar = null,
        status = TeamStatus.ACTIVE,
        visibility = TeamVisibility.PUBLIC,
        createdBy = testUserId,
        createdAt = now,
        updatedAt = now
    )
    
    private val testProject = Project(
        id = testProjectId,
        status = ProjectStatus.ACTIVE,
        updatedAt = now
    )

    private val testBinding = TeamProjectBinding(
        id = testBindingId,
        teamId = testTeamId,
        projectId = testProjectId,
        role = TeamProjectRole.VIEWER,
        boundAt = Clock.System.now(),
        boundBy = testRequesterId
    )

    @Before
    fun setUp() {
        teamRepository = mockk(relaxed = true)
        projectRepository = mockk(relaxed = true)
        bindingRepository = mockk(relaxed = true)
        eventProducer = mockk(relaxed = true)
        bindingService = ProjectBindingServiceImpl(teamRepository, projectRepository, bindingRepository, eventProducer)
    }

    @Test
    fun `bind - success`() = runBlocking {
        coEvery { teamRepository.findById(testTeamId) } returns testTeam
        coEvery { projectRepository.findById(testProjectId) } returns testProject
        coEvery { bindingRepository.exists(testTeamId, testProjectId) } returns false
        coEvery { bindingRepository.bind(any()) } returns testBinding

        val result = bindingService.bind(testTeamId, testProjectId, TeamProjectRole.VIEWER, testRequesterId)

        assertEquals(testProjectId.toString(), result.projectId)
    }

    @Test
    fun `bind - team not found`() = runBlocking {
        coEvery { teamRepository.findById(testTeamId) } returns null

        val ex = assertFailsWith<DomainException.NotFound> {
            bindingService.bind(testTeamId, testProjectId, TeamProjectRole.VIEWER, testRequesterId)
        }
        assertEquals("Team with id '$testTeamId' not found", ex.message)
    }

    @Test
    fun `bind - project not found`() = runBlocking {
        coEvery { teamRepository.findById(testTeamId) } returns testTeam
        coEvery { projectRepository.findById(testProjectId) } returns null

        val ex = assertFailsWith<DomainException.NotFound> {
            bindingService.bind(testTeamId, testProjectId, TeamProjectRole.VIEWER, testRequesterId)
        }
        assertEquals("Project with id '$testProjectId' not found", ex.message)
    }

    @Test
    fun `bind - binding already exists`() = runBlocking {
        coEvery { teamRepository.findById(testTeamId) } returns testTeam
        coEvery { projectRepository.findById(testProjectId) } returns testProject
        coEvery { bindingRepository.exists(testTeamId, testProjectId) } returns true

        val ex = assertFailsWith<DomainException.AlreadyExists> {
            bindingService.bind(testTeamId, testProjectId, TeamProjectRole.VIEWER, testRequesterId)
        }
        assertEquals("Binding already exists", ex.message)
    }

    @Test
    fun `unbind - success`() = runBlocking {
        coEvery { teamRepository.findById(testTeamId) } returns testTeam
        coEvery { projectRepository.findById(testProjectId) } returns testProject

        bindingService.unbind(testTeamId, testProjectId, testRequesterId)

        coVerify { bindingRepository.unbind(testTeamId, testProjectId) }
    }

    @Test
    fun `getBindingsByTeam - success`() = runBlocking {
        coEvery { teamRepository.findById(testTeamId) } returns testTeam
        coEvery { bindingRepository.findByTeam(testTeamId) } returns listOf(testBinding)

        val result = bindingService.getBindingsByTeam(testTeamId)

        assertEquals(1, result.size)
        assertEquals(testProjectId.toString(), result.first().projectId)
    }
}