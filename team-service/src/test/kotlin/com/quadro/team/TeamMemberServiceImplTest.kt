package com.quadro.team

import com.quadro.shared.data.messaging.EventProducer
import com.quadro.shared.dto.DomainException
import com.quadro.team.domain.models.Team
import com.quadro.team.domain.models.TeamMember
import com.quadro.team.domain.models.TeamRole
import com.quadro.team.domain.models.TeamStatus
import com.quadro.team.domain.models.TeamVisibility
import com.quadro.team.domain.repositories.TeamMemberRepository
import com.quadro.team.domain.repositories.TeamRepository
import com.quadro.team.domain.services.TeamMemberService
import com.quadro.team.domain.services.TeamMemberServiceImpl
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.Before
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.time.Clock

@OptIn(ExperimentalCoroutinesApi::class)
class TeamMemberServiceImplTest {
    private lateinit var teamRepository: TeamRepository
    private lateinit var teamMemberRepository: TeamMemberRepository
    private lateinit var eventProducer: EventProducer
    private lateinit var memberService: TeamMemberService

    private val testTeamId = UUID.randomUUID()
    private val testUserId = UUID.randomUUID()
    private val testRequesterId = UUID.randomUUID()
    private val testMemberId = UUID.randomUUID()
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

    private val testMember = TeamMember(
        id = testMemberId,
        teamId = testTeamId,
        userId = testUserId,
        role = TeamRole.MEMBER,
        joinedAt = now,
        invitedBy = testRequesterId,
        invitedAt = now,
        isActive = true
    )

    @Before
    fun setUp() {
        teamRepository = mockk(relaxed = true)
        teamMemberRepository = mockk(relaxed = true)
        eventProducer = mockk(relaxed = true)
        memberService = TeamMemberServiceImpl(teamRepository, teamMemberRepository, eventProducer)
    }

    @Test
    fun `getMembers - success`() = runBlocking {
        coEvery { teamRepository.findById(testTeamId) } returns testTeam
        coEvery { teamMemberRepository.findByTeam(testTeamId) } returns listOf(testMember)

        val result = memberService.getMembers(testTeamId)

        assertEquals(1, result.size)
        assertEquals(testUserId.toString(), result.first().userId)
    }

    @Test
    fun `getMembers - team not found`() = runBlocking {
        coEvery { teamRepository.findById(testTeamId) } returns null

        val ex = assertFailsWith<DomainException.NotFound> {
            memberService.getMembers(testTeamId)
        }
        assertEquals("Team with id '$testTeamId' not found", ex.message)
    }

    @Test
    fun `addMember - user already in team`() = runBlocking {
        coEvery { teamRepository.findById(testTeamId) } returns testTeam
        coEvery { teamMemberRepository.exists(testTeamId, testUserId) } returns true

        val ex = assertFailsWith<DomainException.AlreadyExists> {
            memberService.addMember(testTeamId, testUserId, TeamRole.MEMBER, testRequesterId)
        }
        assertEquals("User in team already exists", ex.message)
    }

    @Test
    fun `removeMember - success`() = runBlocking {
        coEvery { teamRepository.findById(testTeamId) } returns testTeam
        coEvery { teamMemberRepository.exists(testMemberId, testRequesterId) } returns true

        memberService.removeMember(testTeamId, testMemberId, testRequesterId)

        coVerify { teamMemberRepository.remove(testMemberId) }
    }

    @Test
    fun `removeMember - member not found`() = runBlocking {
        coEvery { teamRepository.findById(testTeamId) } returns testTeam
        coEvery { teamMemberRepository.exists(testMemberId, testRequesterId) } returns false

        val ex = assertFailsWith<DomainException.NotFound> {
            memberService.removeMember(testTeamId, testMemberId, testRequesterId)
        }
        assertEquals("User with id '$testRequesterId' not found", ex.message)
    }

    @Test
    fun `changeRole - success`() = runBlocking {
        coEvery { teamRepository.findById(testTeamId) } returns testTeam
        coEvery { teamMemberRepository.exists(testMemberId, testRequesterId) } returns true
        coEvery { teamMemberRepository.updateRole(testMemberId, TeamRole.LEAD) } returns Unit

        memberService.changeRole(testTeamId, testMemberId, TeamRole.LEAD, testRequesterId)

        coVerify { teamMemberRepository.updateRole(testMemberId, TeamRole.LEAD) }
    }
}