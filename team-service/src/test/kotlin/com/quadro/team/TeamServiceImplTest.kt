package com.quadro.team

import com.quadro.shared.data.messaging.EventProducer
import com.quadro.shared.dto.DomainException
import com.quadro.team.domain.models.Team
import com.quadro.team.domain.models.TeamCreate
import com.quadro.team.domain.models.TeamMember
import com.quadro.team.domain.models.TeamRole
import com.quadro.team.domain.models.TeamStatus
import com.quadro.team.domain.models.TeamUpdate
import com.quadro.team.domain.models.TeamVisibility
import com.quadro.team.domain.models.User
import com.quadro.team.domain.models.UserRole
import com.quadro.team.domain.repositories.TeamMemberRepository
import com.quadro.team.domain.repositories.TeamProjectBindingRepository
import com.quadro.team.domain.repositories.TeamRepository
import com.quadro.team.domain.repositories.UserRepository
import com.quadro.team.domain.services.TeamService
import com.quadro.team.domain.services.TeamServiceImpl
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.Before
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.time.Clock

@OptIn(ExperimentalCoroutinesApi::class)
class TeamServiceImplTest {
    private lateinit var teamRepository: TeamRepository
    private lateinit var teamMemberRepository: TeamMemberRepository
    private lateinit var projectBindingRepository: TeamProjectBindingRepository
    private lateinit var userRepository: UserRepository
    private lateinit var eventProducer: EventProducer
    private lateinit var teamService: TeamService

    private val testTeamId = UUID.randomUUID()
    private val testUserId = UUID.randomUUID()
    private val testLeadId = UUID.randomUUID()
    private val testMemberId = UUID.randomUUID()
    private val now = Clock.System.now()

    private val testUser = User(
        id = testUserId,
        role = UserRole.ADMIN,
        isActive = true
    )

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

    private val testTeamMember = TeamMember(
        id = testMemberId,
        teamId = testTeamId,
        userId = testLeadId,
        role = TeamRole.LEAD,
        joinedAt = now,
        invitedBy = testUserId,
        invitedAt = now,
        isActive = true
    )

    @Before
    fun setUp() {
        teamRepository = mockk(relaxed = true)
        teamMemberRepository = mockk(relaxed = true)
        projectBindingRepository = mockk(relaxed = true)
        userRepository = mockk(relaxed = true)
        eventProducer = mockk(relaxed = true)
        teamService = TeamServiceImpl(
            teamRepository,
            teamMemberRepository,
            projectBindingRepository,
            userRepository,
            eventProducer
        )
    }

    // ==================== create ====================

    @Test
    fun `create - success with lead only`() = runBlocking {
        val request = TeamCreate(
            name = "New Team",
            description = null,
            avatar = null,
            visibility = TeamVisibility.PUBLIC,
            leadId = testLeadId,
            initialMembers = null
        )
        val createdTeamId = UUID.randomUUID()

        coEvery { userRepository.findById(testUserId) } returns testUser
        coEvery { teamRepository.existsByName(request.name) } returns false

        val teamSlot = slot<Team>()
        coEvery { teamRepository.create(capture(teamSlot)) } answers {
            teamSlot.captured.copy(id = createdTeamId)
        }

        coEvery { teamMemberRepository.add(any()) } returns testTeamMember.copy(teamId = createdTeamId)
        coEvery { teamMemberRepository.findByTeam(createdTeamId) } returns listOf(
            testTeamMember.copy(teamId = createdTeamId)
        )
        coEvery { projectBindingRepository.findByTeam(createdTeamId) } returns emptyList()

        val result = teamService.create(testUserId, request)

        assertEquals(request.name, result.name)
        assertEquals(1, result.members.size)
    }

    @Test
    fun `create - success with lead and initial members`() = runBlocking {
        val memberId = UUID.randomUUID()
        val secondMemberId = UUID.randomUUID()
        val createdTeamId = UUID.randomUUID()

        val initialMember = TeamMember(
            id = secondMemberId,
            teamId = createdTeamId,
            userId = memberId,
            role = TeamRole.MEMBER,
            joinedAt = now,
            invitedBy = testUserId,
            invitedAt = now,
            isActive = true
        )
        val request = TeamCreate(
            name = "Team",
            description = null,
            avatar = null,
            visibility = TeamVisibility.PUBLIC,
            leadId = testLeadId,
            initialMembers = listOf(memberId)
        )

        coEvery { userRepository.findById(testUserId) } returns testUser
        coEvery { userRepository.findById(memberId) } returns testUser
        coEvery { teamRepository.existsByName(request.name) } returns false

        val teamSlot = slot<Team>()
        coEvery { teamRepository.create(capture(teamSlot)) } answers {
            teamSlot.captured.copy(id = createdTeamId)
        }

        coEvery { teamMemberRepository.add(any()) } returnsMany listOf(
            testTeamMember.copy(teamId = createdTeamId),
            initialMember
        )

        coEvery { teamMemberRepository.findByTeam(createdTeamId) } returns listOf(
            testTeamMember.copy(teamId = createdTeamId),
            initialMember
        )
        coEvery { projectBindingRepository.findByTeam(createdTeamId) } returns emptyList()

        val result = teamService.create(testUserId, request)

        assertNotNull(result)
        assertEquals(2, result.members.size)
    }

    @Test
    fun `create - fails when user not found`() = runBlocking {
        val request = mockk<TeamCreate>()
        coEvery { userRepository.findById(testUserId) } returns null

        val ex = assertFailsWith<DomainException.NotFound> {
            teamService.create(testUserId, request)
        }
        assertEquals("User with id 'ID: $testUserId' not found", ex.message)
    }

    @Test
    fun `create - fails when team name already exists`() = runBlocking {
        val request = TeamCreate(
            name = "Existing",
            leadId = testLeadId
        )
        coEvery { userRepository.findById(testUserId) } returns testUser
        coEvery { teamRepository.existsByName("Existing") } returns true

        val ex = assertFailsWith<DomainException.AlreadyExists> {
            teamService.create(testUserId, request)
        }
        assertEquals("Team 'Existing' already exists", ex.message)
    }

    // ==================== getById ====================

    @Test
    fun `getById - success`() = runBlocking {
        coEvery { teamRepository.findById(testTeamId) } returns testTeam
        coEvery { teamMemberRepository.findByTeam(testTeamId) } returns listOf(testTeamMember)
        coEvery { projectBindingRepository.findByTeam(testTeamId) } returns emptyList()

        val result = teamService.getById(testTeamId)

        assertEquals(testTeam.name, result.name)
        assertEquals(1, result.members.size)
    }

    @Test
    fun `getById - not found`() = runBlocking {
        coEvery { teamRepository.findById(testTeamId) } returns null

        val ex = assertFailsWith<DomainException.NotFound> {
            teamService.getById(testTeamId)
        }
        assertEquals("Team with id '$testTeamId' not found", ex.message)
    }

    // ==================== getAll ====================

    @Test
    fun `getAll - returns paginated list`() = runBlocking {
        val teams = listOf(testTeam)
        coEvery { teamRepository.findAll(1, 10) } returns teams
        coEvery { teamMemberRepository.findByTeam(testTeamId) } returns listOf(testTeamMember)
        coEvery { projectBindingRepository.findByTeam(testTeamId) } returns emptyList()

        val result = teamService.getAll(1, 10)

        assertEquals(1, result.size)
        assertEquals(testTeam.name, result.first().name)
    }

    // ==================== update ====================

    @Test
    fun `update - success`() = runBlocking {
        val request = TeamUpdate(
            name = "Updated Name",
            description = "New Desc",
            avatar = "avatar.png",
            visibility = TeamVisibility.PRIVATE,
            status = TeamStatus.ARCHIVED,
            leadId = null
        )
        coEvery { teamRepository.findById(testTeamId) } returns testTeam
        coEvery { userRepository.findById(testUserId) } returns testUser
        coEvery { teamRepository.update(any()) } answers { firstArg() }
        coEvery { teamMemberRepository.findByTeam(testTeamId) } returns listOf(testTeamMember)
        coEvery { projectBindingRepository.findByTeam(testTeamId) } returns emptyList()

        val result = teamService.update(testTeamId, request, testUserId)

        assertEquals("Updated Name", result.name)
    }

    @Test
    fun `update - fails when team not found`() = runBlocking {
        val request = mockk<TeamUpdate>()
        coEvery { teamRepository.findById(testTeamId) } returns null

        val ex = assertFailsWith<DomainException.NotFound> {
            teamService.update(testTeamId, request, testUserId)
        }
        assertEquals("Team with id '$testTeamId' not found", ex.message)
    }

    // ==================== delete ====================

    @Test
    fun `delete - success by creator`() = runBlocking {
        coEvery { teamRepository.findById(testTeamId) } returns testTeam
        coEvery { userRepository.findById(testUserId) } returns testUser

        teamService.delete(testTeamId, testUserId)

        coVerify { teamRepository.delete(testTeamId) }
    }

    @Test
    fun `delete - fails when not creator`() = runBlocking {
        val anotherUser = UUID.randomUUID()
        coEvery { teamRepository.findById(testTeamId) } returns testTeam
        coEvery { userRepository.findById(anotherUser) } returns testUser

        val ex = assertFailsWith<DomainException.Forbidden> {
            teamService.delete(testTeamId, anotherUser)
        }
        assertEquals("Only creator can delete team", ex.message)
    }
}